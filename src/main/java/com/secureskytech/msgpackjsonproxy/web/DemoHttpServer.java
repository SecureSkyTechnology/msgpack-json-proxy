package com.secureskytech.msgpackjsonproxy.web;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class DemoHttpServer {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    final int port;
    final boolean isHttps;

    private DemoHttpServer(int port, boolean isHttps) {
        this.port = port;
        this.isHttps = isHttps;
    }

    public static DemoHttpServer createHTTP(int port) {
        return new DemoHttpServer(port, false);
    }

    public static DemoHttpServer createHTTPS(int port) {
        return new DemoHttpServer(port, true);
    }

    public Channel start() throws InterruptedException, SSLException, CertificateException {
        SslContext sslCtx;
        if (isHttps) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new DemoHttpServerInitializer(sslCtx));
        return b.bind(port).sync().channel();
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
