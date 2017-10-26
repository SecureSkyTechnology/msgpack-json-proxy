package com.secureskytech.msgpackjsonproxy.proxy;

import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.HTTP_X_SEQUENCE_FLAG_HEADER;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.HTTP_X_SEQUENCE_FLAG_HEADER_VALUE;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.JSON_STD_MIME_TYPE_UTF8;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.MSGPACK_STD_MIME_TYPE;
import static com.secureskytech.msgpackjsonproxy.proxy.Json2MessagePackFilters.HTTP_X_MSGPACK2JSON_FLAG_HEADER;
import static com.secureskytech.msgpackjsonproxy.proxy.Json2MessagePackFilters.HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE;
import static com.secureskytech.msgpackjsonproxy.proxy.MyHttpUtils.readContent;
import static com.secureskytech.msgpackjsonproxy.proxy.MyHttpUtils.updateContent;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter;
import com.secureskytech.msgpackjsonproxy.MsgpackedObjectDumper;
import com.secureskytech.msgpackjsonproxy.sampledata.DemoData;
import com.secureskytech.msgpackjsonproxy.sampledata.GenerateTypicalJsonData;
import com.secureskytech.msgpackjsonproxy.sampledata.GenerateTypicalMessagePackData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;

public class MessagePack2JsonFiltersTest {

    GenerateTypicalJsonData genJsonMapData = new GenerateTypicalJsonData("map");
    GenerateTypicalJsonData genJsonArrayData = new GenerateTypicalJsonData("array");
    GenerateTypicalMessagePackData genMsgpackSequenceData = new GenerateTypicalMessagePackData("sequence");
    GenerateTypicalMessagePackData genMsgpackArrayData = new GenerateTypicalMessagePackData("array");
    GenerateTypicalMessagePackData genMsgpackMapData = new GenerateTypicalMessagePackData("map");

    MessagePack2JsonFilters filter0;
    String before = "";
    String after = "";
    FullHttpRequest fullreq = null;
    FullHttpResponse fullres = null;
    MessagePackJsonConverter conv;

    @Before
    public void prepare() {
        filter0 = new MessagePack2JsonFilters(null, null, true);
        before = "";
        after = "";
        fullreq = null;
        fullres = null;
        conv = new MessagePackJsonConverter();
    }

    @Test
    public void ignoreNonFullHttpRequest() {
        HttpRequest hreq = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        before = hreq.toString();
        assertNull(filter0.proxyToServerRequest(hreq));
        after = hreq.toString();
        assertEquals(before, after);
    }

    @Test
    public void ignoreFullHttpGetRequest() {
        fullreq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        HttpHeaders.setHost(fullreq, "localhost");
        before = fullreq.toString();
        assertNull(filter0.proxyToServerRequest(fullreq));
        after = fullreq.toString();
        assertEquals(before, after);
    }

    @Test
    public void ignoreFullHttpPostRequestButCTisNotMsgpackMime() {
        fullreq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpHeaders.setHost(fullreq, "localhost");
        HttpHeaders.setHeader(fullreq, CONTENT_TYPE, "aaa/bbb");
        before = fullreq.toString();
        assertNull(filter0.proxyToServerRequest(fullreq));
        after = fullreq.toString();
        assertEquals(before, after);
    }

    @Test
    public void convertFullHttpPostRequestMsgpackMapToJson() throws Exception {
        fullreq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpHeaders.setHost(fullreq, "localhost");
        HttpHeaders.setHeader(fullreq, CONTENT_TYPE, MSGPACK_STD_MIME_TYPE);

        byte[] msgpack = genMsgpackMapData.create();
        Object beforeObj = conv.msgpack2object(msgpack);
        ByteArrayOutputStream outs0 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper0 = new MsgpackedObjectDumper(new PrintStream(outs0));
        objDumper0.dump(beforeObj);
        String beforeObjDump = new String(outs0.toByteArray(), StandardCharsets.UTF_8);

        updateContent(fullreq, msgpack);
        before = fullreq.toString();
        assertNull(filter0.proxyToServerRequest(fullreq));
        after = fullreq.toString();
        assertNotEquals(before, after);

        assertEquals(JSON_STD_MIME_TYPE_UTF8, HttpHeaders.getHeader(fullreq, CONTENT_TYPE));
        assertEquals(
            HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE,
            HttpHeaders.getHeader(fullreq, HTTP_X_MSGPACK2JSON_FLAG_HEADER));
        assertNull(HttpHeaders.getHeader(fullreq, HTTP_X_SEQUENCE_FLAG_HEADER));
        String resultJson = readContent(fullreq, StandardCharsets.UTF_8);
        Object afterObj = conv.json2object(resultJson);
        ByteArrayOutputStream outs1 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper1 = new MsgpackedObjectDumper(new PrintStream(outs1));
        objDumper1.dump(afterObj);
        String afterObjDump = new String(outs1.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(beforeObjDump, afterObjDump);
    }

    @Test
    public void convertFullHttpPostRequestMsgpackArrayToJson() throws Exception {
        fullreq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpHeaders.setHost(fullreq, "localhost");
        HttpHeaders.setHeader(fullreq, CONTENT_TYPE, MSGPACK_STD_MIME_TYPE);

        byte[] msgpack = genMsgpackArrayData.create();
        Object beforeObj = conv.msgpack2object(msgpack);
        ByteArrayOutputStream outs0 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper0 = new MsgpackedObjectDumper(new PrintStream(outs0));
        objDumper0.dump(beforeObj);
        String beforeObjDump = new String(outs0.toByteArray(), StandardCharsets.UTF_8);

        updateContent(fullreq, msgpack);
        before = fullreq.toString();
        assertNull(filter0.proxyToServerRequest(fullreq));
        after = fullreq.toString();
        assertNotEquals(before, after);

        assertEquals(JSON_STD_MIME_TYPE_UTF8, HttpHeaders.getHeader(fullreq, CONTENT_TYPE));
        assertEquals(
            HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE,
            HttpHeaders.getHeader(fullreq, HTTP_X_MSGPACK2JSON_FLAG_HEADER));
        assertNull(HttpHeaders.getHeader(fullreq, HTTP_X_SEQUENCE_FLAG_HEADER));
        String resultJson = readContent(fullreq, StandardCharsets.UTF_8);
        Object afterObj = conv.json2object(resultJson);
        ByteArrayOutputStream outs1 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper1 = new MsgpackedObjectDumper(new PrintStream(outs1));
        objDumper1.dump(afterObj);
        String afterObjDump = new String(outs1.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(beforeObjDump, afterObjDump);
    }

    @Test
    public void convertFullHttpPostRequestMsgpackSequenceToJson() throws Exception {
        fullreq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpHeaders.setHost(fullreq, "localhost");
        HttpHeaders.setHeader(fullreq, CONTENT_TYPE, MSGPACK_STD_MIME_TYPE);

        byte[] msgpack = genMsgpackSequenceData.create();
        Object beforeObj = conv.msgpack2object(msgpack);
        ByteArrayOutputStream outs0 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper0 = new MsgpackedObjectDumper(new PrintStream(outs0));
        objDumper0.dump(beforeObj);
        String beforeObjDump = new String(outs0.toByteArray(), StandardCharsets.UTF_8);

        updateContent(fullreq, msgpack);
        before = fullreq.toString();
        assertNull(filter0.proxyToServerRequest(fullreq));
        after = fullreq.toString();
        assertNotEquals(before, after);

        assertEquals(JSON_STD_MIME_TYPE_UTF8, HttpHeaders.getHeader(fullreq, CONTENT_TYPE));
        assertEquals(
            HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE,
            HttpHeaders.getHeader(fullreq, HTTP_X_MSGPACK2JSON_FLAG_HEADER));
        assertEquals(HTTP_X_SEQUENCE_FLAG_HEADER_VALUE, HttpHeaders.getHeader(fullreq, HTTP_X_SEQUENCE_FLAG_HEADER));
        String resultJson = readContent(fullreq, StandardCharsets.UTF_8);
        Object afterObj = conv.json2object(resultJson);
        ByteArrayOutputStream outs1 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper1 = new MsgpackedObjectDumper(new PrintStream(outs1));
        objDumper1.dump(afterObj);
        String afterObjDump = new String(outs1.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(beforeObjDump, afterObjDump);
    }

    @Test
    public void requestConversionErrorResponseEmptyContent() throws Exception {
        fullreq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpHeaders.setHost(fullreq, "localhost");
        HttpHeaders.setHeader(fullreq, CONTENT_TYPE, MSGPACK_STD_MIME_TYPE);
        updateContent(fullreq, DemoData.BYTES_00_FF);
        before = fullreq.toString();
        assertNull(filter0.proxyToServerRequest(fullreq));
        after = fullreq.toString();
        assertNotEquals(before, after);

        // error response has not content.
        assertEquals(MSGPACK_STD_MIME_TYPE, HttpHeaders.getHeader(fullreq, CONTENT_TYPE));
        byte[] r = readContent(fullreq);
        assertEquals(0, r.length);
    }

    void pushMsgpackRequest(byte[] msgpack) {
        fullreq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpHeaders.setHost(fullreq, "localhost");
        HttpHeaders.setHeader(fullreq, CONTENT_TYPE, MSGPACK_STD_MIME_TYPE);
        updateContent(fullreq, msgpack);
        filter0.proxyToServerRequest(fullreq);
    }

    FullHttpResponse createJsonResponse(String json) {
        ByteBuf extendableBuf = Unpooled.buffer();
        byte[] jsonbytes = json.getBytes(StandardCharsets.UTF_8);
        extendableBuf.writeBytes(jsonbytes);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, extendableBuf);
        HttpHeaders.setContentLength(response, jsonbytes.length);
        HttpHeaders.setHeader(response, CONTENT_TYPE, JSON_STD_MIME_TYPE_UTF8);
        return response;
    }

    @Test
    public void responseConversionErrorResponseUnexpected() throws Exception {
        pushMsgpackRequest(genMsgpackMapData.create());

        fullres = this.createJsonResponse(new String(DemoData.BYTES_00_FF, StandardCharsets.UTF_8));
        before = fullres.toString();
        FullHttpResponse fullres2 = (FullHttpResponse) filter0.serverToProxyResponse(fullres);
        after = fullres2.toString();
        assertNotEquals(before, after);

        assertEquals(JSON_STD_MIME_TYPE_UTF8, HttpHeaders.getHeader(fullreq, CONTENT_TYPE));
        // unexpected response content, no-check :P
    }

    @Test
    public void convertFullHttpResponseJsonArrayToMsgpackSequence() throws Exception {
        pushMsgpackRequest(genMsgpackMapData.create());

        String json = genJsonArrayData.create();
        Object beforeObj = conv.json2object(json);
        ByteArrayOutputStream outs0 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper0 = new MsgpackedObjectDumper(new PrintStream(outs0));
        objDumper0.dump(beforeObj);
        String beforeObjDump = new String(outs0.toByteArray(), StandardCharsets.UTF_8);

        fullres = this.createJsonResponse(json);
        HttpHeaders.setHeader(fullres, HTTP_X_SEQUENCE_FLAG_HEADER, HTTP_X_SEQUENCE_FLAG_HEADER_VALUE);
        before = fullres.toString();
        FullHttpResponse fullres2 = (FullHttpResponse) filter0.serverToProxyResponse(fullres);
        after = fullres2.toString();
        assertNotEquals(before, after);

        assertEquals(MSGPACK_STD_MIME_TYPE, HttpHeaders.getHeader(fullres2, CONTENT_TYPE));
        byte[] msgpack = readContent(fullres2);
        Object afterObj = conv.msgpack2object(msgpack);
        assertTrue(conv.isSequence());
        ByteArrayOutputStream outs1 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper1 = new MsgpackedObjectDumper(new PrintStream(outs1));
        objDumper1.dump(afterObj);
        String afterObjDump = new String(outs1.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(beforeObjDump, afterObjDump);
    }

    @Test
    public void convertFullHttpResponseJsonArrayToMsgpackArray() throws Exception {
        pushMsgpackRequest(genMsgpackMapData.create());

        String json = genJsonArrayData.create();
        Object beforeObj = conv.json2object(json);
        ByteArrayOutputStream outs0 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper0 = new MsgpackedObjectDumper(new PrintStream(outs0));
        objDumper0.dump(beforeObj);
        String beforeObjDump = new String(outs0.toByteArray(), StandardCharsets.UTF_8);

        fullres = this.createJsonResponse(json);
        before = fullres.toString();
        FullHttpResponse fullres2 = (FullHttpResponse) filter0.serverToProxyResponse(fullres);
        after = fullres2.toString();
        assertNotEquals(before, after);

        assertEquals(MSGPACK_STD_MIME_TYPE, HttpHeaders.getHeader(fullres2, CONTENT_TYPE));
        byte[] msgpack = readContent(fullres2);
        Object afterObj = conv.msgpack2object(msgpack);
        assertFalse(conv.isSequence());
        ByteArrayOutputStream outs1 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper1 = new MsgpackedObjectDumper(new PrintStream(outs1));
        objDumper1.dump(afterObj);
        String afterObjDump = new String(outs1.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(beforeObjDump, afterObjDump);
    }

    @Test
    public void convertFullHttpResponseJsonMapToMsgpackMap() throws Exception {
        pushMsgpackRequest(genMsgpackMapData.create());

        String json = genJsonMapData.create();
        Object beforeObj = conv.json2object(json);
        ByteArrayOutputStream outs0 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper0 = new MsgpackedObjectDumper(new PrintStream(outs0));
        objDumper0.dump(beforeObj);
        String beforeObjDump = new String(outs0.toByteArray(), StandardCharsets.UTF_8);

        fullres = this.createJsonResponse(json);
        before = fullres.toString();
        FullHttpResponse fullres2 = (FullHttpResponse) filter0.serverToProxyResponse(fullres);
        after = fullres2.toString();
        assertNotEquals(before, after);

        assertEquals(MSGPACK_STD_MIME_TYPE, HttpHeaders.getHeader(fullres2, CONTENT_TYPE));
        byte[] msgpack = readContent(fullres2);
        Object afterObj = conv.msgpack2object(msgpack);
        assertFalse(conv.isSequence());
        ByteArrayOutputStream outs1 = new ByteArrayOutputStream();
        MsgpackedObjectDumper objDumper1 = new MsgpackedObjectDumper(new PrintStream(outs1));
        objDumper1.dump(afterObj);
        String afterObjDump = new String(outs1.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(beforeObjDump, afterObjDump);
    }

    @Test
    public void skipResponseConversionByCT() throws Exception {
        pushMsgpackRequest(genMsgpackMapData.create());

        String json = genJsonMapData.create();
        fullres = this.createJsonResponse(json);
        HttpHeaders.setHeader(fullres, CONTENT_TYPE, "aaa/bbb");
        before = fullres.toString();
        FullHttpResponse fullres2 = (FullHttpResponse) filter0.serverToProxyResponse(fullres);
        after = fullres2.toString();
        // untouched.
        assertEquals(before, after);
    }

    @Test
    public void skipResponseConversionByNotFullHttpResponse() throws Exception {
        pushMsgpackRequest(genMsgpackMapData.create());

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpHeaders.setContentLength(response, 0);
        HttpHeaders.setHeader(response, CONTENT_TYPE, JSON_STD_MIME_TYPE_UTF8);
        before = response.toString();
        HttpResponse res2 = (HttpResponse) filter0.serverToProxyResponse(response);
        after = res2.toString();
        // untouched.
        assertEquals(before, after);
    }

    @Test
    public void skipResponseConversionByRequest() throws Exception {
        // SKIP : pushMsgpackRequest(genMsgpackMapData.create());
        String json = genJsonMapData.create();
        fullres = this.createJsonResponse(json);
        before = fullres.toString();
        FullHttpResponse fullres2 = (FullHttpResponse) filter0.serverToProxyResponse(fullres);
        after = fullres2.toString();
        // untouched.
        assertEquals(before, after);
    }

    @Test
    public void skipResponseConversionByConstructorArg() throws Exception {
        filter0 = new MessagePack2JsonFilters(null, null, false);

        pushMsgpackRequest(genMsgpackMapData.create());
        String json = genJsonMapData.create();
        fullres = this.createJsonResponse(json);
        before = fullres.toString();
        FullHttpResponse fullres2 = (FullHttpResponse) filter0.serverToProxyResponse(fullres);
        after = fullres2.toString();
        // untouched.
        assertEquals(before, after);
    }

}
