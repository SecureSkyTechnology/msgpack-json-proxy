package com.secureskytech.msgpackjsonproxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ApacheHttpdMimeTypesTest {

    @Test
    public void test() {
        ApacheHttpdMimeTypes mt = ApacheHttpdMimeTypes.defaultMimeTypes;
        assertEquals("", mt.getExtension(null));
        assertEquals("", mt.getExtension(""));
        assertEquals("", mt.getExtension("a"));
        assertEquals("", mt.getExtension("."));
        assertEquals("", mt.getExtension("a."));
        assertEquals("a", mt.getExtension("a.a"));
        assertEquals("a", mt.getExtension(".a"));
        assertEquals("txt", mt.getExtension("test.txt"));
        assertEquals("html", mt.getExtension("/index.html"));
        assertEquals("html", mt.getExtension("/abc/def/ghi.html_backup"));
        assertEquals("jsp", mt.getExtension("/abc/def/ghi.jsp?aaa=bbb"));
        assertEquals("jsp", mt.getExtension("/abc/def/ghi.jsp;jsessionid=xxxx"));
        assertEquals("js", mt.getExtension("/jquery-3.2.1.min.js"));

        assertEquals("", mt.getMimeType(null));
        assertEquals("", mt.getMimeType(""));
        assertEquals("", mt.getMimeType("aaaaaaaaaaaaaaaaaaaaa"));

        assertEquals("text/html", mt.getMimeType("html"));
        assertEquals("text/html", mt.getMimeType("HTML"));
        assertEquals("text/plain", mt.getMimeType("txt"));
        assertEquals("text/csv", mt.getMimeType("csv"));
        assertEquals("text/css", mt.getMimeType("css"));

        assertEquals("image/jpeg", mt.getMimeType("jpg"));
        assertEquals("image/jpeg", mt.getMimeType("jpeg"));
        assertEquals("image/gif", mt.getMimeType("gif"));
        assertEquals("image/png", mt.getMimeType("png"));
        assertEquals("image/bmp", mt.getMimeType("bmp"));
        assertEquals("image/x-icon", mt.getMimeType("ico"));

        assertEquals("application/javascript", mt.getMimeType("js"));
        assertEquals("application/json", mt.getMimeType("json"));
        assertEquals("application/font-woff", mt.getMimeType("woff"));

        assertEquals("application/xml", mt.getMimeType("xml"));
        assertEquals("application/xml", mt.getMimeType("xsl"));
        assertEquals("application/xml-dtd", mt.getMimeType("dtd"));
        assertEquals("application/xhtml+xml", mt.getMimeType("xhtml"));
        assertEquals("application/atom+xml", mt.getMimeType("atom"));
        assertEquals("application/rsd+xml", mt.getMimeType("rsd"));
        assertEquals("application/rss+xml", mt.getMimeType("rss"));
        assertEquals("application/pdf", mt.getMimeType("pdf"));
        assertEquals("application/x-tar", mt.getMimeType("tar"));
    }
}
