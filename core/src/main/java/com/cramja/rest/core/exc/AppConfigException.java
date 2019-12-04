package com.cramja.rest.core.exc;

public class AppConfigException extends IllegalStateException {

    public AppConfigException() {
    }

    public AppConfigException(String s) {
        super(s);
    }

    public AppConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
