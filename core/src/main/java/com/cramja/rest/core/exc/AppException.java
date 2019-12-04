package com.cramja.rest.core.exc;

import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract HttpResponseStatus getStatus();

}
