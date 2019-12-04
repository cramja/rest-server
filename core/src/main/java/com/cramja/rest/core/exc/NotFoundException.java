package com.cramja.rest.core.exc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.NOT_FOUND;
    }
}
