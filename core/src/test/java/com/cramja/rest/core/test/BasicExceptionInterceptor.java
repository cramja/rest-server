package com.cramja.rest.core.test;

import com.cramja.rest.core.exc.AppException;
import com.cramja.rest.core.route.Interceptor;
import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.util.Either;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicExceptionInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(BasicExceptionInterceptor.class);

    @Override
    public Either<HttpReqCtx, HttpResCtx> intercept(HttpReqCtx req) {
        return Either.ofL(req); // pass through
    }

    @Override
    public Optional<HttpResCtx> intercept(RuntimeException e) {
        HttpResCtx.HttpResCtxBuilder builder = HttpResCtx.builder();
        if (e instanceof AppException) {
            AppException appException = (AppException) e;
            builder.status(appException.getStatus());
            builder.body(String.format("{'error':'%s','message':'%s'}", appException.getStatus().reasonPhrase(), e.getMessage()).replace('\'', '"'));

        } else {
            String trace = UUID.randomUUID().toString();
            logger.warn("unhandled exception, trace: {}, {}", trace, e);
            builder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            builder.body(String.format("{'message':'internal error','trace':'%s'}", trace).replace('\'', '"'));

        }
        return Optional.of(builder.build());
    }
}
