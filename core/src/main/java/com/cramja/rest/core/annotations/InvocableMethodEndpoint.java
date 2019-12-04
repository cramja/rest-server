package com.cramja.rest.core.annotations;

import com.cramja.rest.core.exc.AppConfigException;
import com.cramja.rest.core.exc.ServerError;
import com.cramja.rest.core.route.Endpoint;
import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

public class InvocableMethodEndpoint implements Endpoint {

    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    final Object serviceImpl;
    final Method method;
    final List<Function<HttpReqCtx, Object>> extractArgs;
    final int code;

    public InvocableMethodEndpoint(
            Object serviceImpl,
            Method method,
            List<Function<HttpReqCtx, Object>> extractArgs,
            int code
    ) {
        this.serviceImpl = serviceImpl;
        this.method = method;
        this.extractArgs = extractArgs;
        this.code = code;
    }

    @Override
    public HttpResCtx accept(HttpReqCtx req) {
        final Object returnVal;
        try {
            returnVal = method.invoke(serviceImpl, argsForInvocation(req));
        } catch (IllegalAccessException e) {
            throw new AppConfigException("unable to bind to service method", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new AppConfigException("bound method threw checked exception", e.getCause());
            }
        }
        HttpResCtx.HttpResCtxBuilder res = HttpResCtx.builder();
        if (returnVal instanceof String) {
            res.body((String) returnVal);
        } else if (returnVal != null) {
            res.body(serialize(returnVal));
        } else {
            res.body("");
        }
        return res.status(HttpResponseStatus.valueOf(code)).build();
    }

    private Object[] argsForInvocation(HttpReqCtx req) {
        Object[] args = new Object[extractArgs.size()];
        for (int i = 0; i < extractArgs.size(); i++) {
            args[i] = extractArgs.get(i).apply(req);
        }
        return args;
    }

    private String serialize(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new ServerError("unable to serialize response", e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s::%s(%d)", serviceImpl.getClass().getName(), method.getName(), extractArgs.size());
    }
}
