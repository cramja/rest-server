package com.cramja.rest.core.exc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ServerError extends AppException {

    public ServerError(String message) {
        super(message);
    }

    public ServerError(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
