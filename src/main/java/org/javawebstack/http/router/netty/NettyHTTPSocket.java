package org.javawebstack.http.router.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.javawebstack.http.router.HTTPMethod;
import org.javawebstack.http.router.adapter.IHTTPSocket;

import java.io.*;
import java.util.*;

public class NettyHTTPSocket implements IHTTPSocket {

    private final ChannelHandlerContext context;
    private final FullHttpRequest request;
    private int status = 200;
    private String message = "OK";
    private final HttpHeaders httpHeaders = new DefaultHttpHeaders();
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public NettyHTTPSocket(ChannelHandlerContext context, FullHttpRequest request) {
        this.context = context;
        this.request = request;
    }

    public InputStream getInputStream() throws IOException {
        ByteBuf content = request.content();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return new ByteArrayInputStream(bytes);
    }

    public OutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    public void close() throws IOException {
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(status);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, Unpooled.copiedBuffer(outputStream.toByteArray()));
        response.headers().add(httpHeaders);
        context.writeAndFlush(response);
        context.close();
    }

    public boolean isClosed() {
        return !context.channel().isOpen();
    }

    public IHTTPSocket setResponseStatus(int status, String message) {
        this.status = status;
        this.message = message;
        return this;
    }

    public IHTTPSocket setResponseHeader(String name, String value) {
        httpHeaders.set(name, value);
        return this;
    }

    public IHTTPSocket addResponseHeader(String name, String value) {
        httpHeaders.add(name, value);
        return this;
    }

    public HTTPMethod getRequestMethod() {
        return HTTPMethod.valueOf(request.method().name());
    }

    public String getRequestPath() {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        return decoder.path();
    }

    public String getRequestQuery() {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        return decoder.rawQuery();
    }

    public String getRequestVersion() {
        return request.protocolVersion().text();
    }

    public Set<String> getRequestHeaderNames() {
        return request.headers().names();
    }

    public List<String> getRequestHeaders(String name) {
        return request.headers().getAll(name);
    }

    public int getResponseStatus() {
        return status;
    }

    public String getResponseStatusMessage() {
        return message;
    }

    public void writeHeaders() throws IOException {
        context.flush();
    }

    public String getRemoteAddress() {
        return context.channel().remoteAddress().toString();
    }
}
