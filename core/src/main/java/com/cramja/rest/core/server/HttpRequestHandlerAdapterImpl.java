package com.cramja.rest.core.server;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static java.nio.charset.Charset.defaultCharset;

import com.cramja.rest.core.util.Helpers;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestHandlerAdapterImpl extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandlerAdapterImpl.class);

    private Function<HttpReqCtx, HttpResCtx> handler;

    public HttpRequestHandlerAdapterImpl(Function<HttpReqCtx, HttpResCtx> handler) {
        this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            super.channelRead(ctx, msg);
            return;
        }

        final FullHttpRequest request = (FullHttpRequest) msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        HttpReqCtx.HttpReqCtxBuilder reqCtxBuilder = HttpReqCtx.builder()
                .method(request.method().name())
                .pathParams(new HashMap<>())
                .headers(request.headers())
                .path(getPath(decoder))
                .queryParams(decoder.parameters());

        if (request.headers().getInt(HttpHeaderNames.CONTENT_LENGTH, 0) > 0) {
            final String requestBody = request.content().toString(defaultCharset());
            reqCtxBuilder.body(requestBody);
        } else {
            reqCtxBuilder.body("");
        }

        HttpResCtx resCtx = handler.apply(reqCtxBuilder.build());

        final byte[] bytes = resCtx.getBody().getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                resCtx.getStatus(),
                copiedBuffer(bytes)
        );

        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(
                    HttpHeaderNames.CONNECTION,
                    HttpHeaderValues.KEEP_ALIVE
            );
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        // TODO: content-length header should not be set in the case of certain statuses
        // https://tools.ietf.org/html/rfc7230#section-3.3.1
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);

        ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("unhandled exception", cause);
        ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(String.format("{\"message\":\"%s\"}", cause.getMessage()).getBytes())
        ));
        ctx.disconnect();
    }

    private List<String> getPath(QueryStringDecoder decoder) {
        final String path = Helpers.canonicalPath(decoder.path());
        return Arrays.asList(path.split("/"));
    }
}
