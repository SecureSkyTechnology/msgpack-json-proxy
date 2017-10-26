package com.secureskytech.msgpackjsonproxy.sampledata;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.jackson.dataformat.MessagePackExtensionType;

import com.secureskytech.msgpackjsonproxy.HexDumper;

import lombok.Data;

@Data
public class DemoPojo {
    Object nil = null;
    Integer int0 = Integer.valueOf(0);
    Integer int1 = Integer.valueOf(1);
    Integer intmin = Integer.valueOf(Integer.MIN_VALUE);
    Integer intmax = Integer.valueOf(Integer.MAX_VALUE);
    Long long0 = Long.valueOf(0L);
    Long long1 = Long.valueOf(1L);
    Long longmin = Long.valueOf(Long.MIN_VALUE);
    Long longmax = Long.valueOf(Long.MAX_VALUE);
    Float float00 = Float.valueOf(0.0f);
    Float float10 = Float.valueOf(1.0f);
    Float floatmin = Float.valueOf(Float.MIN_VALUE);
    Float floatmax = Float.valueOf(Float.MAX_VALUE);
    Double double00 = Double.valueOf(0.0);
    Double double10 = Double.valueOf(1.0);
    Double doublemin = Double.valueOf(Double.MIN_VALUE);
    Double doublemax = Double.valueOf(Double.MAX_VALUE);
    Boolean booleanTrue = Boolean.valueOf(true);
    Boolean booleanFalse = Boolean.valueOf(false);
    String strfield = "A日本語1";
    byte[] bytes00toFF = DemoData.BYTES_00_FF;
    List<Object> arrayList = Arrays.asList(null, Integer.valueOf(0), "B日本語2");
    Map<String, Object> map;
    MessagePackExtensionType mpext = new MessagePackExtensionType((byte) -120, DemoData.BYTES_00_FF);

    public DemoPojo() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("C日本語3", null);
        m.put("D日本語4", Integer.valueOf(0));
        map = m;
    }

    @Override
    public String toString() {
        HexDumper dumper = new HexDumper();
        dumper.setPrefix("0x");
        dumper.setSeparator(",");
        dumper.setToUpperCase(true);
        return "DemoPojo [nil="
            + nil
            + ", int0="
            + int0
            + ", int1="
            + int1
            + ", intmin="
            + intmin
            + ", intmax="
            + intmax
            + ", long0="
            + long0
            + ", long1="
            + long1
            + ", longmin="
            + longmin
            + ", longmax="
            + longmax
            + ", float00="
            + float00
            + ", float10="
            + float10
            + ", floatmin="
            + floatmin
            + ", floatmax="
            + floatmax
            + ", double00="
            + double00
            + ", double10="
            + double10
            + ", doublemin="
            + doublemin
            + ", doublemax="
            + doublemax
            + ", booleanTrue="
            + booleanTrue
            + ", booleanFalse="
            + booleanFalse
            + ", strfield="
            + strfield
            + ", bytes00toFF="
            + dumper.dump(bytes00toFF)
            + ", arrayList="
            + arrayList
            + ", map="
            + map
            + ", mpext={type:"
            + mpext.getType()
            + ", data:"
            + dumper.dump(mpext.getData())
            + "}"
            + "]";
    }
}
