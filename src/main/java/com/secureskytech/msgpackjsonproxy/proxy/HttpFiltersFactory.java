package com.secureskytech.msgpackjsonproxy.proxy;

import org.littleshoot.proxy.HttpFilters;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public interface HttpFiltersFactory {
    HttpFilters create(HttpRequest originalRequest, ChannelHandlerContext ctx);
}
