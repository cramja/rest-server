package com.cramja.rest.core.route;

import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.util.Either;
import java.util.Optional;

public class InterceptorDecoratorAdapter extends AbstractRouteNodeDecorator {

    final Interceptor interceptor;

    public InterceptorDecoratorAdapter(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public Either<HttpReqCtx, HttpResCtx> process(HttpReqCtx req) {
        return interceptor.intercept(req);
    }

    @Override
    public Optional<HttpResCtx> handleException(RuntimeException e) {
        return interceptor.intercept(e);
    }
}
