package com.secureskytech.msgpackjsonproxy.sampledata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.jackson.dataformat.MessagePackExtensionType;

public class DemoData {

    public static final byte[] BYTES_00_FF = new byte[256];
    static {
        for (int i = 0; i < BYTES_00_FF.length; i++) {
            BYTES_00_FF[i] = (byte) i;
        }
    }

    public static List<Object> asList() throws IOException {
        List<Object> list = new ArrayList<>();
        setTypicalTypeData(list);
        return list;
    }

    public static Map<Object, Object> asMap() throws IOException {
        List<Object> list = new ArrayList<>();
        setTypicalTypeData(list);
        Map<Object, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < list.size() / 2; i++) {
            Object o1 = list.get(i * 2);
            Object o2 = list.get(i * 2 + 1);
            map.put(o1, o2);
        }
        return map;
    }

    static void setTypicalTypeData(List<Object> d) throws IOException {
        d.add("nil");
        d.add(null);
        d.add("int0");
        d.add(Integer.valueOf(0));
        d.add("int1");
        d.add(Integer.valueOf(1));
        d.add("intmin");
        d.add(Integer.valueOf(Integer.MIN_VALUE));
        d.add("intmax");
        d.add(Integer.valueOf(Integer.MAX_VALUE));
        d.add("long0");
        d.add(Long.valueOf(0L));
        d.add("long1");
        d.add(Long.valueOf(1L));
        d.add("longmin");
        d.add(Long.valueOf(Long.MIN_VALUE));
        d.add("longmax");
        d.add(Long.valueOf(Long.MAX_VALUE));
        d.add("float0.0");
        d.add(Float.valueOf(0.0f));
        d.add("float1.0");
        d.add(Float.valueOf(1.0f));
        d.add("floatmin");
        d.add(Float.valueOf(Float.MIN_VALUE));
        d.add("floatmax");
        d.add(Float.valueOf(Float.MAX_VALUE));
        d.add("double0.0");
        d.add(Double.valueOf(0.0));
        d.add("double1.0");
        d.add(Double.valueOf(1.0));
        d.add("doublemin");
        d.add(Double.valueOf(Double.MIN_VALUE));
        d.add("doublemax");
        d.add(Double.valueOf(Double.MAX_VALUE));
        d.add("boolean_true");
        d.add(Boolean.valueOf(true));
        d.add("boolean_false");
        d.add(Boolean.valueOf(false));
        d.add("abcdefg");
        d.add("A日本語1");
        d.add("0x00_to_0xFF");
        d.add(BYTES_00_FF);
        d.add("array");
        d.add(Arrays.asList(null, Integer.valueOf(0), "B日本語2"));
        d.add("map");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("C日本語3", null);
        m.put("D日本語4", Integer.valueOf(0));
        d.add(m);
        d.add("ext");
        MessagePackExtensionType mpext = new MessagePackExtensionType((byte) -120, BYTES_00_FF);
        d.add(mpext);
    }
}
