package com.cramja.rest.core.exc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class AuthenticationException extends AppException {

    private String privateMessage;

    public AuthenticationException() {
        super("");
    }

    public AuthenticationException(String publicMessage) {
        super(publicMessage);
    }

    public AuthenticationException(String publicMessage, Throwable cause) {
        super(publicMessage, cause);
    }

    public AuthenticationException withPrivateMessage(String privateMessage) {
        this.privateMessage = privateMessage;
        return this;
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.FORBIDDEN;
    }

    public String getPrivateMessage() {
        return privateMessage;
    }
}
