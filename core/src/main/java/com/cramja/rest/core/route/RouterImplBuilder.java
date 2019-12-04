package com.cramja.rest.core.route;

import static java.util.Arrays.asList;

import com.cramja.rest.core.exc.AppConfigException;
import com.cramja.rest.core.util.Pair;
import com.cramja.rest.core.util.Helpers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterImplBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RouterImplBuilder.class);

    private Map<Route, Endpoint> routes = new HashMap<>();
    private List<Pair<String, Interceptor>> interceptors = new ArrayList<>();

    public RouterImplBuilder addInterceptor(String path, Interceptor interceptor) {
        interceptors.add(Pair.of(Helpers.canonicalPath(path), interceptor));
        return this;
    }

    public RouterImplBuilder addRoutes(Map<Route, Endpoint> routes) {
        for (Route key : routes.keySet()) {
            if (this.routes.containsKey(key)) {
                throw new AppConfigException("duplicate route added for " + key.toString());
            }
            this.routes.put(key, routes.get(key));
        }
        return this;
    }

    public Endpoint build() {
        RouteNode root = new RouteNodeImpl("", 0);

        for (Entry<Route, Endpoint> entry : routes.entrySet()) {
            final Route route = entry.getKey();
            root.addMethod(
                    route,
                    0,
                    entry.getValue());
        }

        for (Pair<String, Interceptor> e : interceptors) {
            List<String> pathComponents = asList(e.left().split("/"));
            root = root.addDecorator(pathComponents, 0, new InterceptorDecoratorAdapter(e.right()));
        }

        logger.debug("{}", root.describe());

        return root;
    }
}
