package com.cramja.crypto.web;

import com.cramja.rest.core.exc.AppException;
import com.cramja.rest.core.route.Interceptor;
import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.util.Either;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Optional;

public class ExceptionInterceptor implements Interceptor {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Either<HttpReqCtx, HttpResCtx> intercept(HttpReqCtx req) {
        return Either.ofL(req);
    }

    @Override
    public Optional<HttpResCtx> intercept(RuntimeException e) {
        if (e instanceof AppException) {
            AppException appExc = (AppException) e;
            String body = serialize(new ErrorMessage(appExc.getMessage()));
            return Optional.of(HttpResCtx.builder()
                    .body(body)
                    .status(appExc.getStatus())
                    .build());
        } else {
            String body = serialize(new ErrorMessage(e.getMessage()));
            return Optional.of(HttpResCtx.builder()
                    .body(body)
                    .status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    private String serialize(ErrorMessage errorMessage) {
        try {
            return mapper.writeValueAsString(errorMessage);
        } catch (JsonProcessingException e) {
            return "{\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    static class ErrorMessage {
        String message;

        public ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


}
