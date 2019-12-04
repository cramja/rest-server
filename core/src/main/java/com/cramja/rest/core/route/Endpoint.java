package com.cramja.rest.core.route;

import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;

public interface Endpoint {

    HttpResCtx accept(HttpReqCtx req);

}
