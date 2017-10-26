package com.secureskytech.msgpackjsonproxy.sampledata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

public class GenerateTypicalMessagePackData {
    final String toplevel;

    public GenerateTypicalMessagePackData(final String toplevel) {
        switch (toplevel) {
        case "sequence":
        case "array":
        case "map":
            this.toplevel = toplevel;
            break;
        default:
            throw new IllegalArgumentException("Unknown tolevel. use sequence/array/map.");
        }
    }

    public byte[] create() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MessagePacker packer = MessagePack.newDefaultPacker(out);
        if ("array".equals(this.toplevel)) {
            packer.packArrayHeader(48);
        } else if ("map".equals(this.toplevel)) {
            packer.packMapHeader(24);
        }
        packTypicalTypeData(packer);
        packer.flush();

        byte[] r = out.toByteArray();
        out.close();
        return r;
    }

    void packTypicalTypeData(MessagePacker packer) throws IOException {
        //1
        packer.packString("nil");
        packer.packNil();
        //2
        packer.packString("int0");
        packer.packInt(0);
        //3
        packer.packString("int1");
        packer.packInt(1);
        //4
        packer.packString("intmin");
        packer.packInt(Integer.MIN_VALUE);
        //5
        packer.packString("intmax");
        packer.packInt(Integer.MAX_VALUE);
        //6
        packer.packString("long0");
        packer.packLong(0L);
        //7
        packer.packString("long1");
        packer.packLong(1L);
        //8
        packer.packString("longmin");
        packer.packLong(Long.MIN_VALUE);
        //9
        packer.packString("longmax");
        packer.packLong(Long.MAX_VALUE);
        //10
        packer.packString("float0.0");
        packer.packFloat(0.0f);
        //11
        packer.packString("float1.0");
        packer.packFloat(1.0f);
        //12
        packer.packString("floatmin");
        packer.packFloat(Float.MIN_VALUE);
        //13
        packer.packString("floatmax");
        packer.packFloat(Float.MAX_VALUE);
        //14
        packer.packString("double0.0");
        packer.packDouble(0.0);
        //15
        packer.packString("double1.0");
        packer.packDouble(1.0);
        //16
        packer.packString("doublemin");
        packer.packDouble(Double.MIN_VALUE);
        //17
        packer.packString("doublemax");
        packer.packDouble(Double.MAX_VALUE);
        //18
        packer.packString("boolean_true");
        packer.packBoolean(true);
        //19
        packer.packString("boolean_false");
        packer.packBoolean(false);
        //20
        packer.packString("abcdefg");
        packer.packString("A日本語1");
        //21
        packer.packString("0x00_to_0xFF");
        packer.packBinaryHeader(DemoData.BYTES_00_FF.length);
        packer.writePayload(DemoData.BYTES_00_FF);
        //22
        packer.packString("array");
        packer.packArrayHeader(3);
        packer.packNil();
        packer.packInt(0);
        packer.packString("B日本語2");
        //23
        packer.packString("map");
        packer.packMapHeader(2);
        packer.packString("C日本語3");
        packer.packNil();
        packer.packString("D日本語4");
        packer.packInt(0);
        //24
        packer.packString("ext");
        packer.packExtensionTypeHeader((byte) 0, DemoData.BYTES_00_FF.length);
        packer.writePayload(DemoData.BYTES_00_FF);
    }
}
