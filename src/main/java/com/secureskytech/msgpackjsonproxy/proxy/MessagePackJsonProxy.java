package com.secureskytech.msgpackjsonproxy.proxy;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Queue;

import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;

public class MessagePackJsonProxy {
    static final ImpersonatingMitmManager mitmManager =
        ImpersonatingMitmManager.builder().trustAllServers(true).build();

    private static HttpProxyServer createProxy(final int port, final InetSocketAddress upstream,
            final HttpFiltersFactory httpFiltersFactory) {
        HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrap();
        bootstrap.withAddress(new InetSocketAddress("0.0.0.0", port));
        bootstrap.withManInTheMiddle(mitmManager);
        if (Objects.nonNull(upstream)) {
            final ChainedProxy chainedProxy = new ChainedProxyAdapter() {
                @Override
                public InetSocketAddress getChainedProxyAddress() {
                    return upstream;
                }
            };
            bootstrap.withChainProxyManager(new ChainedProxyManager() {
                @Override
                public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
                    chainedProxies.add(chainedProxy);
                }
            });
        }
        bootstrap.withFiltersSource(new GenericHttpFiltersSource(httpFiltersFactory));
        return bootstrap.start();
    }

    public static HttpProxyServer createMsgpack2JsonProxy(final int port, final InetSocketAddress upstream,
            final boolean enableResponseConversion) {
        return createProxy(port, upstream, new HttpFiltersFactory() {
            @Override
            public HttpFilters create(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new MessagePack2JsonFilters(originalRequest, ctx, enableResponseConversion);
            }
        });
    }

    public static HttpProxyServer createJson2MsgpackProxy(final int port, final InetSocketAddress upstream,
            final boolean enableResponseConversion) {
        return createProxy(port, upstream, new HttpFiltersFactory() {
            @Override
            public HttpFilters create(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new Json2MessagePackFilters(originalRequest, ctx, enableResponseConversion);
            }
        });
    }
}
