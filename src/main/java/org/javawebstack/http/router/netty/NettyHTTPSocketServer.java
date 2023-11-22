package org.javawebstack.http.router.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.javawebstack.http.router.adapter.IHTTPSocketHandler;
import org.javawebstack.http.router.adapter.IHTTPSocketServer;

import java.io.IOException;

public class NettyHTTPSocketServer implements IHTTPSocketServer {

    protected int port;
    protected int maxThreads;
    protected ChannelFuture closeFuture;
    protected EventLoopGroup masterGroup;
    protected EventLoopGroup slaveGroup;
    protected IHTTPSocketHandler handler;


    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setMaxThreads(int  maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void start() throws IOException {
        masterGroup = new NioEventLoopGroup(maxThreads);
        slaveGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();

        b.group(masterGroup, slaveGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpServerInitializer(handler));

        try {
            Channel channel = b.bind(port).sync().channel();
            closeFuture = channel.closeFuture();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        masterGroup.shutdownGracefully();
        slaveGroup.shutdownGracefully();
    }

    public void join() {
        try {
            closeFuture.sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            masterGroup.shutdownGracefully();
            slaveGroup.shutdownGracefully();
        }
    }

    public void setHandler(IHTTPSocketHandler handler) {
        this.handler = handler;
    }

    public boolean isWebSocketSupported() {
        return true;
    }

}
