package com.secureskytech.msgpackjsonproxy.proxy;

import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.HTTP_X_SEQUENCE_FLAG_HEADER;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.HTTP_X_SEQUENCE_FLAG_HEADER_VALUE;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.JSON_STD_MIME_TYPE_UTF8;
import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.MSGPACK_STD_MIME_TYPE;
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

public class Json2MessagePackFilters extends HttpFiltersAdapter {
    public static final String HTTP_X_MSGPACK2JSON_FLAG_HEADER = "X-msgpack2json-converted";
    public static final String HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE = "1";

    private static Logger LOG = LoggerFactory.getLogger(Json2MessagePackFilters.class);
    final HexDumper hexDumper = HexDumper.create0xCommaDumper();
    boolean isConversionNeeded = false;
    final boolean enableResponseConversion;

    public Json2MessagePackFilters(HttpRequest originalRequest, ChannelHandlerContext ctx,
            final boolean enableResponseConversion) {
        super(originalRequest, ctx);
        this.enableResponseConversion = enableResponseConversion;
        LOG.debug("json2msgpack proxy : enableResponseConversion = {}", enableResponseConversion);
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        if (!(httpObject instanceof FullHttpRequest)) {
            return null;
        }
        FullHttpRequest fhr = (FullHttpRequest) httpObject;
        HttpHeaders headers = fhr.headers();
        final String requestContentType = headers.get(CONTENT_TYPE);
        if (!MessagePackJsonConverter.isJsonMimeType(requestContentType)) {
            LOG.debug("json2msgpack proxy [request] : skipped for Content-Type: {}", requestContentType);
            return null;
        }
        this.isConversionNeeded =
            headers.contains(HTTP_X_MSGPACK2JSON_FLAG_HEADER)
                && HTTP_X_MSGPACK2JSON_FLAG_HEADER_VALUE.equals(headers.get(HTTP_X_MSGPACK2JSON_FLAG_HEADER));
        if (!this.isConversionNeeded) {
            LOG.debug("json2msgpack proxy [request] : skipped for missing msgpack header");
            return null;
        }
        final boolean isSequence =
            headers.contains(HTTP_X_SEQUENCE_FLAG_HEADER)
                && HTTP_X_SEQUENCE_FLAG_HEADER_VALUE.equals(headers.get(HTTP_X_SEQUENCE_FLAG_HEADER));
        LOG.debug(
            "json2msgpack proxy [request] : convert target json detected(sequenced={}), Content-Type: {}",
            isSequence,
            requestContentType);
        final ByteBuf requestBodyByteBuf = fhr.content();
        final byte[] jsonRequestBodyBytes = new byte[requestBodyByteBuf.readableBytes()];
        requestBodyByteBuf.readBytes(jsonRequestBodyBytes);
        final String json = new String(jsonRequestBodyBytes, StandardCharsets.UTF_8);
        LOG.debug("json2msgpack proxy [request] : dump json in request body: {}", json);
        MessagePackJsonConverter converter = new MessagePackJsonConverter();
        try {
            final byte[] msgpack = converter.json2msgpack(json, isSequence);
            LOG.debug(
                "json2msgpack proxy [request] : dump converted messagepack(sequenced={}): {}",
                isSequence,
                hexDumper.dump(msgpack));
            final ByteBuf msgpackByteBuf = Unpooled.copiedBuffer(msgpack);
            requestBodyByteBuf.clear().writeBytes(msgpackByteBuf);
            HttpHeaders.setContentLength(fhr, requestBodyByteBuf.readableBytes());
            HttpHeaders.setHeader(fhr, CONTENT_TYPE, MSGPACK_STD_MIME_TYPE);
        } catch (Exception e) {
            LOG.warn("json2msgpack proxy [request] : json -> msgpack conversion error", e);
        }
        return null;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (!this.enableResponseConversion) {
            return httpObject;
        }
        if (!this.isConversionNeeded) {
            LOG.debug("json2msgpack proxy [response] : skipped (request body was NOT msgpack2jsoned json)");
            return httpObject;
        }
        if (!(httpObject instanceof FullHttpResponse)) {
            return httpObject;
        }
        FullHttpResponse fhr = (FullHttpResponse) httpObject;
        HttpHeaders headers = fhr.headers();
        final String responseContentType = headers.get(CONTENT_TYPE);
        if (!MessagePackJsonConverter.isMessagePackMimeType(responseContentType)) {
            LOG.debug("json2msgpack proxy [response] : skipped for Content-Type: {}", responseContentType);
            return httpObject;
        }
        final ByteBuf responseBodyByteBuf = fhr.content();
        final byte[] msgpackedResponseBodyBytes = new byte[responseBodyByteBuf.readableBytes()];
        responseBodyByteBuf.readBytes(msgpackedResponseBodyBytes);
        LOG.debug(
            "json2msgpack proxy [response] : dump messagepack in response body: {}",
            hexDumper.dump(msgpackedResponseBodyBytes));
        MessagePackJsonConverter converter = new MessagePackJsonConverter();
        try {
            final String json = converter.msgpack2json(msgpackedResponseBodyBytes);
            LOG.debug(
                "json2msgpack proxy [response] : dump converted json (sequenced={}) : {}",
                converter.isSequence(),
                json);
            final ByteBuf jsonByteBuf = Unpooled.copiedBuffer(json, StandardCharsets.UTF_8);
            responseBodyByteBuf.clear().writeBytes(jsonByteBuf);
            HttpHeaders.setContentLength(fhr, responseBodyByteBuf.readableBytes());
            HttpHeaders.setHeader(fhr, CONTENT_TYPE, JSON_STD_MIME_TYPE_UTF8);
            if (converter.isSequence()) {
                HttpHeaders.setHeader(fhr, HTTP_X_SEQUENCE_FLAG_HEADER, HTTP_X_SEQUENCE_FLAG_HEADER_VALUE);
            }
        } catch (Exception e) {
            LOG.warn("json2msgpack proxy [response] : msgpack -> json conversion error", e);
        }
        return fhr;
    }
}
