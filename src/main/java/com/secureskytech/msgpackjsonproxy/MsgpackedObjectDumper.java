package com.secureskytech.msgpackjsonproxy;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.msgpack.jackson.dataformat.MessagePackExtensionType;

import com.google.common.base.Strings;

public class MsgpackedObjectDumper {

    final HexDumper hexDumper = HexDumper.create0xCommaDumper();
    final PrintStream writer;
    final String indentStr;
    int indent = 0;

    public MsgpackedObjectDumper(final PrintStream writer) {
        this(writer, "  ");
    }

    public MsgpackedObjectDumper(final PrintStream writer, final String indentStr) {
        this.writer = writer;
        this.indentStr = indentStr;
    }

    void print(String s) {
        writer.print(s);
    }

    void println(String s) {
        writer.println(s);
    }

    void printi(String s) {
        writer.print(Strings.repeat(indentStr, indent));
        print(s);
    }

    void printiln(String s) {
        writer.print(Strings.repeat(indentStr, indent));
        writer.println(s);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void dump(Object srcObj) {
        if (srcObj instanceof List) {
            print("\r\n");
            printiln(srcObj.getClass().getName() + "#[");
            indent++;
            List list = (List) srcObj;
            for (int i = 0; i < list.size(); i++) {
                printi("<" + i + "> : ");
                Object obj = list.get(i);
                this.dump(obj);
            }
            indent--;
            printiln("]");
        } else if (srcObj instanceof Map) {
            print("\r\n");
            printiln(srcObj.getClass().getName() + "#{");
            indent++;
            Map<Object, Object> srcMap = (Map<Object, Object>) srcObj;
            int i = 0;
            for (Map.Entry<Object, Object> e : srcMap.entrySet()) {
                printi("<" + i + ">[K] : ");
                this.dump(e.getKey());
                printi("<" + i + ">[V] : ");
                this.dump(e.getValue());
                i++;
            }
            indent--;
            printiln("}");
        } else if (srcObj instanceof MessagePackExtensionType) {
            MessagePackExtensionType ext = (MessagePackExtensionType) srcObj;
            println("MessagePackExtensionType#[");
            indent++;
            printiln("<type>:" + ext.getType());
            printiln("<data>:" + hexDumper.dump(ext.getData()));
            printiln("]");
            indent--;
        } else if (srcObj instanceof byte[]) {
            byte[] ba = (byte[]) srcObj;
            println("byte[]#" + hexDumper.dump(ba));
        } else if (Objects.isNull(srcObj)) {
            println("null");
        } else {
            println(srcObj.getClass().getName() + "#" + srcObj.toString());
        }
    }
}
