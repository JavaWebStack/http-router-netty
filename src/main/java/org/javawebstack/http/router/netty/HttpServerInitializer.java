package org.javawebstack.http.router.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import org.javawebstack.http.router.adapter.IHTTPSocketHandler;
import org.javawebstack.http.router.util.websocket.WebSocketUtil;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private final IHTTPSocketHandler handler;

    public HttpServerInitializer(IHTTPSocketHandler handler) {
        this.handler = handler;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast("aggregator", new HttpObjectAggregator(165536));
        p.addLast(new HttpServerExpectContinueHandler());
        p.addLast(new SimpleChannelInboundHandler<HttpObject>() {
            protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                if (msg instanceof FullHttpRequest) {
                    NettyHTTPSocket httpSocket = new NettyHTTPSocket(ctx, (FullHttpRequest) msg);
                    if (((FullHttpRequest) msg).headers().contains("sec-websocket-key")) {
                        if (!WebSocketUtil.accept(httpSocket, null)) {
                            ctx.close();
                            return;
                        }
                    }

                    handler.handle(httpSocket);
                    ctx.flush();
                    ctx.close();
                }
            }
        });
    }

}
