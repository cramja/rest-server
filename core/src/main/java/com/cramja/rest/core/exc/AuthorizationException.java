package com.cramja.rest.core.exc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class AuthorizationException extends AppException {

    private String privateMessage;

    public AuthorizationException() {
        super("");
    }

    public AuthorizationException(String publicMessage) {
        super(publicMessage);
    }

    public AuthorizationException(String publicMessage, Throwable cause) {
        super(publicMessage, cause);
    }

    public AuthorizationException withPrivateMessage(String privateMessage) {
        this.privateMessage = privateMessage;
        return this;
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.UNAUTHORIZED;
    }

    public String getPrivateMessage() {
        return privateMessage;
    }
}
