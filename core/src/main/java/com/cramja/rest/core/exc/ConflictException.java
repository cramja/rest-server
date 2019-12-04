package com.cramja.rest.core.exc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.CONFLICT;
    }
}
