package com.secureskytech.msgpackjsonproxy.web;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.DATE;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

/**
 * copied from http://netty.io/4.0/xref/io/netty/example/http/file/HttpStaticFileServerHandler.html
 * 
 * customized by:
 * @author sakamoto@securesky-tech.com
 */
public class DemoHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static Logger LOG = LoggerFactory.getLogger(DemoHttpServerHandler.class);
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        try {
            DemoController controller = new DemoController(request);
            FullHttpResponse response = controller.dispatch();
            setDateHeader(response);
            if (HttpHeaders.isKeepAlive(request)) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            } else {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (IllegalArgumentException e) {
            LOG.debug("illegal parameter", e);
            sendError(ctx, BAD_REQUEST, "illegal parameter, see server log");
        } catch (URISyntaxException e) {
            LOG.debug("request uri parse error", e);
            sendError(ctx, BAD_REQUEST, "request uri parser error");
        } catch (UnsupportedOperationException e) {
            sendError(ctx, NOT_FOUND);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("exceptionCaught", cause);
        String msg = cause.getMessage();
        String st = Throwables.getStackTraceAsString(cause);
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR, msg + "\r\n" + st);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        sendError(ctx, status, "Failure: " + status + "\r\n");
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String errorResponseBody) {
        FullHttpResponse response =
            new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(errorResponseBody, CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response HTTP response
     */
    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));
    }
}
