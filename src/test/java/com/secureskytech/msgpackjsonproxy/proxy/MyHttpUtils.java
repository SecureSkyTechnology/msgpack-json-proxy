package com.secureskytech.msgpackjsonproxy.proxy;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;

public class MyHttpUtils {
    public static void updateContent(FullHttpMessage fhm, String json) {
        updateContent(fhm, json, StandardCharsets.UTF_8);
    }

    public static void updateContent(FullHttpMessage fhm, String s, Charset cs) {
        ByteBuf body = fhm.content();
        ByteBuf newbody = Unpooled.copiedBuffer(s, cs);
        body.clear().writeBytes(newbody);
        HttpHeaders.setContentLength(fhm, body.readableBytes());
    }

    public static void updateContent(FullHttpMessage fhm, byte[] data) {
        ByteBuf body = fhm.content();
        ByteBuf newbody = Unpooled.copiedBuffer(data);
        body.clear().writeBytes(newbody);
        HttpHeaders.setContentLength(fhm, body.readableBytes());
    }

    public static byte[] readContent(HttpContent hc) {
        ByteBuf body = hc.content();
        byte[] r = new byte[body.readableBytes()];
        body.readBytes(r);
        return r;
    }

    public static String readContent(HttpContent hc, Charset cs) {
        return new String(readContent(hc), cs);
    }
}
