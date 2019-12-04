package com.cramja.rest.core.server;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HttpResCtx {

    HttpResponseStatus status;
    HttpHeaders headers;
    String body;

}
