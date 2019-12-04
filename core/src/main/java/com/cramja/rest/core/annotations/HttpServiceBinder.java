package com.cramja.rest.core.annotations;

import com.cramja.rest.core.exc.AppConfigException;
import com.cramja.rest.core.route.Endpoint;
import com.cramja.rest.core.route.Route;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:
 * - support multiple service interfaces.
 */
public class HttpServiceBinder {
    private static final Logger logger = LoggerFactory.getLogger(HttpServiceBinder.class);

    final Class<?> serviceInterface;
    final Object service;

    Map<Route, Endpoint> routes;
    String serviceBasePath;

    public HttpServiceBinder(Object service) {
        Class<?> serviceInterface = null;
        for (Class<?> iclass : service.getClass().getInterfaces()) {
            if (iclass.getAnnotation(Service.class) != null) {
                if (serviceInterface != null) {
                    throw new AppConfigException("inheriting multiple service interfaces is not supported");
                }
                serviceInterface = iclass;
            }
        }

        if (serviceInterface == null) {
            throw new AppConfigException(
                    "HttpServiceBinder only binds classes annotated with @Service. " +
                            service.getClass() + " was not annotated");
        }

        this.service = service;
        this.serviceInterface = serviceInterface;
    }

    public Map<Route, Endpoint> getBindings() {
        if (routes != null) {
            return routes;
        }
        routes = new HashMap<>();

        Path basePathAnno = serviceInterface.getAnnotation(Path.class);
        if (basePathAnno != null) {
            this.serviceBasePath = basePathAnno.value();
            // ignore method
        }

        for (Method m : serviceInterface.getMethods()) {
            Path methodPathAnno = m.getAnnotation(Path.class);
            if (methodPathAnno == null) {
                continue;
            }
            HttpMethodBinder binder = new HttpMethodBinder(this, m);
            routes.put(binder.getRoute(), binder.getEndpoint());
        }

        return routes;
    }

    String getBasePath() {
        return serviceBasePath;
    }

    Object getService() {
        return service;
    }
}
