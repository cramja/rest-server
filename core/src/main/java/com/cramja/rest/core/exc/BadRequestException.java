package com.cramja.rest.core.exc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class BadRequestException extends AppException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }
}
