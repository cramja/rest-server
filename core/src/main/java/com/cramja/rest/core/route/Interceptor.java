package com.cramja.rest.core.route;

import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.util.Either;
import java.util.Optional;

public interface Interceptor {

    /**
     * Intercept an HttpRequest, optionally returning a response.
     * @param req incoming request.
     * @return Either HttpReqCtx or HttpResCtx. A response which is present indicates
     * that the request should return immediately with the given response. Otherwise
     * the returned HttpReqCtx will be passed forward through the router.
     */
    Either<HttpReqCtx, HttpResCtx> intercept(HttpReqCtx req);

    /**
     * Intercept an exception thrown somewhere lower in the call stack.
     * @return Optional Response. Empty indicates ignore.
     */
    default Optional<HttpResCtx> intercept(RuntimeException e) {
        return Optional.empty();
    }

}
