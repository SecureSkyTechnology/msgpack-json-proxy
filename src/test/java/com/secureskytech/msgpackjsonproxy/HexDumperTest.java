package com.secureskytech.msgpackjsonproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HexDumperTest {

    static final byte[] BYTES_00_FF;
    static {
        BYTES_00_FF = new byte[256];
        for (int i = 0; i <= 0xFF; i++) {
            BYTES_00_FF[i] = (byte) i;
        }
    }

    String createDummy(String prefix, boolean toUpperCase, String separator) {
        char seeds[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        StringBuilder r = new StringBuilder();
        int k = 0;
        for (int i = 0; i < seeds.length; i++) {
            for (int j = 0; j < seeds.length; j++, k++) {
                StringBuilder sb = new StringBuilder();
                sb.append(seeds[i]);
                sb.append(seeds[j]);
                String t = sb.toString();
                if (toUpperCase) {
                    t = t.toUpperCase();
                }
                r.append(prefix);
                r.append(t);
                if (k < (BYTES_00_FF.length - 1)) {
                    r.append(separator);
                }
            }
        }
        return r.toString();
    }

    @Test
    public void testTypicalInput() {
        HexDumper dumper = new HexDumper();
        assertEquals("", dumper.dump(null));
        assertEquals("", dumper.dump(new byte[0]));
        assertEquals("00", dumper.dump(new byte[] { 0x00 }));
        assertEquals("00ff", dumper.dump(new byte[] { 0x00, (byte) 0xFF }));
        assertEquals("007fff", dumper.dump(new byte[] { 0x00, (byte) 0x7F, (byte) 0xFF }));
    }

    @Test
    public void testSimple() {
        HexDumper dumper = new HexDumper();
        String r = dumper.dump(BYTES_00_FF);
        assertEquals(r, createDummy("", false, ""));
    }

    @Test
    public void testSimplePrefix() {
        HexDumper dumper = new HexDumper();
        dumper.setPrefix("0x");
        String r = dumper.dump(BYTES_00_FF);
        assertEquals(r, createDummy("0x", false, ""));
    }

    @Test
    public void testSimpleUpper() {
        HexDumper dumper = new HexDumper();
        dumper.setToUpperCase(true);
        String r = dumper.dump(BYTES_00_FF);
        assertEquals(r, createDummy("", true, ""));
    }

    @Test
    public void testSimpleSeparator() {
        HexDumper dumper = new HexDumper();
        dumper.setSeparator(",");
        String r = dumper.dump(BYTES_00_FF);
        assertEquals(r, createDummy("", false, ","));
    }

    @Test
    public void testPrefixUpperSeparator() {
        HexDumper dumper = new HexDumper();
        dumper.setPrefix("0x");
        dumper.setToUpperCase(true);
        dumper.setSeparator(", ");
        String r = dumper.dump(BYTES_00_FF);
        assertEquals(r, createDummy("0x", true, ", "));
    }
}
