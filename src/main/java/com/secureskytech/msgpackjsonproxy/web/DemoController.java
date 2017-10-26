package com.secureskytech.msgpackjsonproxy.web;

import static com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter.HTTP_X_SEQUENCE_FLAG_HEADER;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.secureskytech.msgpackjsonproxy.ApacheHttpdMimeTypes;
import com.secureskytech.msgpackjsonproxy.HexDumper;
import com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter;
import com.secureskytech.msgpackjsonproxy.sampledata.DemoPojo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

public class DemoController {
    private static final Logger LOG = LoggerFactory.getLogger(DemoController.class);

    final FullHttpRequest request;
    final URI uri;
    final String path;
    final HttpHeaders headers;
    final String requestContentType;
    final byte[] requestBody;
    final HexDumper dumper;

    public DemoController(FullHttpRequest request) throws URISyntaxException {
        this.request = request;
        this.uri = new URI(request.getUri());
        this.path = uri.getPath();
        this.headers = request.headers();
        this.requestContentType = headers.get(CONTENT_TYPE);
        ByteBuf requestBodyByteBuf = request.content();
        this.requestBody = new byte[requestBodyByteBuf.readableBytes()];
        requestBodyByteBuf.readBytes(this.requestBody);
        this.dumper = new HexDumper();
        dumper.setPrefix("0x");
        dumper.setSeparator(",");
        dumper.setToUpperCase(true);
    }

    public FullHttpResponse dispatch() throws UnsupportedOperationException, IllegalArgumentException, IOException {
        switch (path) {
        case "/echoasis":
            return echoAsIs();
        case "/getpojo":
            return getPojo();
        case "/echopojo":
            return echoPojo();
        default:
            try {
                return staticResource();
            } catch (Exception e) {
                LOG.debug("static resource falldown error", e);
                throw new UnsupportedOperationException("path[" + path + "] is not supported.");
            }
        }
    }

    FullHttpResponse staticResource() throws IOException {
        String basename = "/".equals(path) ? "/index.html" : path;
        byte[] contents = Resources.toByteArray(Resources.getResource("demohttpserver" + basename));
        final String ext = ApacheHttpdMimeTypes.defaultMimeTypes.getExtension(basename);
        String mimetype = ApacheHttpdMimeTypes.defaultMimeTypes.getMimeType(ext);
        if (mimetype.startsWith("text/") || "application/javascript".equals(mimetype)) {
            mimetype = mimetype + "; charset=utf-8";
        }
        LOG.debug("staticResource for {} found, ext={}, mimetype detected as {}", basename, ext, mimetype);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(contents));
        HttpHeaders.setContentLength(response, contents.length);
        HttpHeaders.setHeader(response, CONTENT_TYPE, mimetype);
        return response;
    }

    FullHttpResponse echoAsIs() {
        LOG.info("echoasis : Content-Type={}, requestBody={}", requestContentType, dumper.dump(requestBody));

        String responseContentType = "application/octet-stream";
        if (!Strings.isNullOrEmpty(requestContentType)) {
            responseContentType = headers.get(CONTENT_TYPE);
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(this.requestBody));
        HttpHeaders.setContentLength(response, this.requestBody.length);
        HttpHeaders.setHeader(response, CONTENT_TYPE, responseContentType);

        // echo msgpack sequence type side-channel transfer.
        if (headers.contains(HTTP_X_SEQUENCE_FLAG_HEADER)) {
            HttpHeaders.setHeader(response, HTTP_X_SEQUENCE_FLAG_HEADER, headers.get(HTTP_X_SEQUENCE_FLAG_HEADER));
        }

        return response;
    }

    FullHttpResponse getPojo() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        DemoPojo getPojo = new DemoPojo();
        LOG.info("getpojo : pojo={}", getPojo);

        byte[] pojoBytes = objectMapper.writeValueAsBytes(getPojo);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(pojoBytes));
        HttpHeaders.setContentLength(response, pojoBytes.length);
        HttpHeaders.setHeader(response, CONTENT_TYPE, MessagePackJsonConverter.MSGPACK_STD_MIME_TYPE);
        return response;
    }

    FullHttpResponse echoPojo() throws IllegalArgumentException, JsonParseException, JsonMappingException, IOException {
        if (Strings.isNullOrEmpty(requestContentType)
            || !requestContentType.startsWith(MessagePackJsonConverter.MSGPACK_STD_MIME_TYPE)) {
            throw new IllegalArgumentException("Content-Type is not " + MessagePackJsonConverter.MSGPACK_STD_MIME_TYPE);
        }
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        DemoPojo requestPojo = objectMapper.readValue(requestBody, DemoPojo.class);
        LOG.info("echopojo : received pojo={}", requestPojo);

        byte[] pojoBytes = objectMapper.writeValueAsBytes(requestPojo);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(pojoBytes));
        HttpHeaders.setContentLength(response, pojoBytes.length);
        HttpHeaders.setHeader(response, CONTENT_TYPE, MessagePackJsonConverter.MSGPACK_STD_MIME_TYPE);
        return response;
    }
}
