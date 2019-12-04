package com.cramja.rest.core.route;

import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import java.util.function.Function;

public interface RequestHandler extends Function<HttpReqCtx, HttpResCtx> {

    HttpResCtx apply(HttpReqCtx req);

}
