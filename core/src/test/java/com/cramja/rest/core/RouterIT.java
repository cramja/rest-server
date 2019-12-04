package com.cramja.rest.core;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

import com.cramja.rest.core.annotations.HttpServiceBinder;
import com.cramja.rest.core.route.Endpoint;
import com.cramja.rest.core.route.RouterImplBuilder;
import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.test.BasicExceptionInterceptor;
import com.cramja.rest.core.test.Entity;
import com.cramja.rest.core.test.EntityServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterIT {
    private static final Logger logger = LoggerFactory.getLogger(RouterIT.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private EntityServiceImpl service;

    private Endpoint endpoint;
    private BasicExceptionInterceptor interceptor;

    @Before
    public void before() {
        service = new EntityServiceImpl();
        interceptor = new BasicExceptionInterceptor();
        endpoint = new RouterImplBuilder()
                .addRoutes(new HttpServiceBinder(service).getBindings())
                .addInterceptor("", interceptor)
                .build();
        logger.info("{}", endpoint);
    }

    @Test
    public void route_whenBodyParam_thenMarshals() {
        final UUID id = DataHelpers.nextId();
        Entity e = new Entity(id, randomUUID().toString());
        service.pushReturnValue(e);
        HttpReqCtx req = HttpReqCtx.builder()
                .method("PUT")
                .path(Arrays.asList("", "entities", id.toString()))
                .body(ser(e))
                .build();

        HttpResCtx res = endpoint.accept(req);

        Entity returned = deser(res.getBody(), Entity.class);
        assertEquals(e, returned);
        assertEquals(e, service.popArg());
        assertEquals(id, service.popArg());
        assertEquals(HttpResponseStatus.CREATED, res.getStatus());
    }

    private static String ser(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T deser(String s, Class<T> clz) {
        try {
            return objectMapper.readValue(s, clz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
