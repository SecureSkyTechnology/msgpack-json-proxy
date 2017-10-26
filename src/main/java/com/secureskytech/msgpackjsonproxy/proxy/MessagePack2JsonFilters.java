package com.secureskytech.msgpackjsonproxy.proxy;

import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.HTTP_X_SEQUENCE_FLAG_HEADER;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.HTTP_X_SEQUENCE_FLAG_HEADER_VALUE;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.JSON_STD_MIME_TYPE_UTF8;
import static com.secureskytech.msgpackjsonproxy.proxy.Json2MessagePackFilters.HTTP_X_MSGPACK2JSON_FLAG_HEADER;
import static com.secureskytech.msgpackjsonproxy.proxy.Json2MessagePackFilters.HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.nio.charset.StandardCharsets;

import org.littleshoot.proxy.HttpFiltersAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.secureskytech.msgpackjsonproxy.HexDumper;
import com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class MessagePack2JsonFilters extends HttpFiltersAdapter {
    private static Logger LOG = LoggerFactory.getLogger(MessagePack2JsonFilters.class);
    final HexDumper hexDumper = HexDumper.create0xCommaDumper();
    boolean requestWasMsgpacked = false;
    final boolean enableResponseConversion;

    public MessagePack2JsonFilters(HttpRequest originalRequest, ChannelHandlerContext ctx,
            final boolean enableResponseConversion) {
        super(originalRequest, ctx);
        this.enableResponseConversion = enableResponseConversion;
        LOG.debug("msgpack2json proxy : enableResponseConversion = {}", enableResponseConversion);
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        if (!(httpObject instanceof FullHttpRequest)) {
            return null;
        }
        FullHttpRequest fhr = (FullHttpRequest) httpObject;
        HttpHeaders headers = fhr.headers();
        final String requestContentType = headers.get(CONTENT_TYPE);
        if (!MessagePackJsonConverter.isMessagePackMimeType(requestContentType)) {
            LOG.debug("msgpack2json proxy [request] : skipped for Content-Type: {}", requestContentType);
            return null;
        }
        LOG.debug("msgpack2json proxy [request] : messagepack detected, Content-Type: {}", requestContentType);
        this.requestWasMsgpacked = true;
        final ByteBuf requestBodyByteBuf = fhr.content();
        final byte[] msgpackedRequestBodyBytes = new byte[requestBodyByteBuf.readableBytes()];
        requestBodyByteBuf.readBytes(msgpackedRequestBodyBytes);
        LOG.debug(
            "msgpack2json proxy [request] : dump messagepack in request body: {}",
            hexDumper.dump(msgpackedRequestBodyBytes));
        MessagePackJsonConverter converter = new MessagePackJsonConverter();
        try {
            final String json = converter.msgpack2json(msgpackedRequestBodyBytes);
            LOG.debug(
                "msgpack2json proxy [request] : dump converted json (sequenced={}) : {}",
                converter.isSequence(),
                json);
            final ByteBuf jsonByteBuf = Unpooled.copiedBuffer(json, StandardCharsets.UTF_8);
            requestBodyByteBuf.clear().writeBytes(jsonByteBuf);
            HttpHeaders.setContentLength(fhr, requestBodyByteBuf.readableBytes());
            HttpHeaders.setHeader(fhr, CONTENT_TYPE, JSON_STD_MIME_TYPE_UTF8);
            HttpHeaders.setHeader(fhr, HTTP_X_MSGPACK2JSON_FLAG_HEADER, HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE);
            if (converter.isSequence()) {
                HttpHeaders.setHeader(fhr, HTTP_X_SEQUENCE_FLAG_HEADER, HTTP_X_SEQUENCE_FLAG_HEADER_VALUE);
            }
        } catch (Exception e) {
            LOG.warn("msgpack2json proxy [request] : msgpack -> json conversion error", e);
        }
        return null;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (!this.enableResponseConversion) {
            return httpObject;
        }
        if (!this.requestWasMsgpacked) {
            LOG.debug("msgpack2json proxy [response] : skipped (request body was NOT msgpacked)");
            return httpObject;
        }
        if (!(httpObject instanceof FullHttpResponse)) {
            return httpObject;
        }
        FullHttpResponse fhr = (FullHttpResponse) httpObject;
        HttpHeaders headers = fhr.headers();
        final String responseContentType = headers.get(CONTENT_TYPE);
        if (!MessagePackJsonConverter.isJsonMimeType(responseContentType)) {
            LOG.debug("msgpack2json proxy [response] : skipped for Content-Type: {}", responseContentType);
            return httpObject;
        }
        boolean isSequence =
            headers.contains(HTTP_X_SEQUENCE_FLAG_HEADER)
                && HTTP_X_SEQUENCE_FLAG_HEADER_VALUE.equals(headers.get(HTTP_X_SEQUENCE_FLAG_HEADER));
        LOG.debug(
            "msgpack2json proxy [response] : json detected (sequenced={}), Content-Type: {}",
            isSequence,
            responseContentType);
        final ByteBuf responseBodyByteBuf = fhr.content();
        final byte[] jsonRequestBodyBytes = new byte[responseBodyByteBuf.readableBytes()];
        responseBodyByteBuf.readBytes(jsonRequestBodyBytes);
        final String json = new String(jsonRequestBodyBytes, StandardCharsets.UTF_8);
        LOG.debug("msgpack2json proxy [response] : dump json in response body: {}", json);
        MessagePackJsonConverter converter = new MessagePackJsonConverter();
        try {
            final byte[] msgpack = converter.json2msgpack(json, isSequence);
            LOG.debug(
                "msgpack2json proxy [response] : dump converted messagepack(sequenced={}): {}",
                isSequence,
                hexDumper.dump(msgpack));
            final ByteBuf msgpackByteBuf = Unpooled.copiedBuffer(msgpack);
            responseBodyByteBuf.clear().writeBytes(msgpackByteBuf);
            HttpHeaders.setContentLength(fhr, responseBodyByteBuf.readableBytes());
            HttpHeaders.setHeader(fhr, CONTENT_TYPE, MessagePackJsonConverter.MSGPACK_STD_MIME_TYPE);
        } catch (Exception e) {
            LOG.warn("msgpack2json proxy [response] : json -> msgpack conversion error", e);
        }
        return fhr;
    }
}
