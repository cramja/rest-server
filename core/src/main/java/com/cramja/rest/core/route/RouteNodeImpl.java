package com.cramja.rest.core.route;

import static java.lang.String.join;

import com.cramja.rest.core.exc.AppConfigException;
import com.cramja.rest.core.exc.NotFoundException;
import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.util.Helpers;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class RouteNodeImpl implements RouteNode {

    private String name;
    private int pathIndex;

    private Map<String, RouteNode> nextRouter = new HashMap<>();
    private Map<String, Endpoint> methodRouter = new HashMap<>();

    RouteNodeImpl(String name, int pathIndex) {
        this.name = name;
        this.pathIndex = pathIndex;
    }

    public String getName() {
        return name;
    }
    
    public void addMethod(
            Route route,
            int index,
            Endpoint endpoint) {
        if (route.getNormalizedPath().length - 1 == index) {
            methodRouter.put(route.getMethod(), endpoint);
            return;
        }

        String nextName = route.getNormalizedPath()[index + 1];
        if (isVariable(nextName)) {
            if (nextRouter.containsKey("*")) {
                RouteNode nextNode = nextRouter.get("*");
                nextNode.addMethod(route, index + 1, endpoint);

            } else {
                Helpers.checkState(nextRouter.isEmpty(), "variable path already has mapping");

                RouteNode node = new RouteNodeImpl(nextName, index + 1);
                node.addMethod(route, index + 1, endpoint);

                final String varName = route.getPathVarName(index + 1);
                PathExtractorDecorator decorator = new PathExtractorDecorator(node, varName, index + 1);
                nextRouter.put("*", decorator);

            }

        } else {
            if (nextRouter.containsKey(nextName)) {
                RouteNode next = nextRouter.get(nextName);
                next.addMethod(route, index + 1, endpoint);
            } else {
                RouteNodeImpl node = new RouteNodeImpl(nextName, index + 1);
                node.addMethod(route, index + 1, endpoint);
                nextRouter.put(node.getName(), node);
            }
        }
    }

    public RouteNode addDecorator(
            List<String> path,
            int index,
            RouteNodeDecorator decorator) {
        if (path.size() - 1 == index) {
            // base case, decorate this node
            decorator.setDecoratee(this);
            return decorator;
        }

        final RouteNode next;
        String nextName = path.get(index + 1);
        String key = nextName;
        if (isVariable(nextName)) {
            key = "*";
        }
        if (nextRouter.containsKey(key)) {
            next = nextRouter.get(key);
        } else {
            throw new AppConfigException("decorated non-existent path " + join("/", path));
        }

        nextRouter.put(key, next.addDecorator(path, index + 1, decorator));
        return this;
    }

    @Override
    public HttpResCtx accept(HttpReqCtx req) {
        if (pathIndex + 1 < req.getPath().size()) {
            String nextComponent = req.getPath().get(pathIndex + 1);
            Endpoint next = nextRouter.getOrDefault(nextComponent, null);
            if (next == null && nextRouter.containsKey("*")) {
                next = nextRouter.get("*"); // variable node
            }

            if (next == null) {
                throw notFound(req);
            }
            return next.accept(req);

        }

        // terminal
        Endpoint handler = methodRouter.getOrDefault(req.getMethod(), null);
        if (handler == null) {
            throw notFound(req);
        } else {
            return handler.accept(req);
        }
    }

    protected NotFoundException notFound(HttpReqCtx req) {
        return new NotFoundException(String.format("%s %s",
                req.getMethod(),
                join("/", req.getPath())));
    }

    @Override
    public String toString() {
        return "RouteNodeImpl{" +
                "name='" + name + '\'' +
                ", pathIndex=" + pathIndex +
                '}';
    }

    public String describe() {
        int longestPath = 0;
        int longestMethod = 0;
        List<RouteDebugInfo> infos = getRouteDebugInfo();
        for (RouteDebugInfo info : infos) {
            final int plength = info.getPath().length();
            if (longestPath < plength) {
                longestPath = plength;
            }

            final int mlength = info.getMethod().length();
            if (longestMethod < mlength) {
                longestMethod = mlength;
            }
        }

        final String fmt = "%n%-" + longestMethod + "s %-" + longestPath + "s %s";
        StringBuilder sb = new StringBuilder();
        for (RouteDebugInfo info : infos) {
            sb.append(String.format(fmt, info.getMethod(), info.getPath(), info.getTerminal()));
        }
        return sb.toString();
    }

    List<RouteDebugInfo> getRouteDebugInfo() {
        return getRouteDebugInfo(new LinkedList<>());
    }

    private List<RouteDebugInfo> getRouteDebugInfo(LinkedList<String> pathSoFar) {
        pathSoFar.add(getName());
        LinkedList<RouteDebugInfo> resolved = new LinkedList<>();
        if (!methodRouter.isEmpty()) {
            String pathStr = join("/", pathSoFar);
            for (String method : methodRouter.keySet()) {
                resolved.add(new RouteDebugInfo(method, pathStr, methodRouter.get(method)));
            }
        }
        for (RouteNode routeNode : nextRouter.values()) {
            while (routeNode instanceof RouteNodeDecorator) {
                routeNode = ((RouteNodeDecorator) routeNode).getDecoratee();
            }
            resolved.addAll(((RouteNodeImpl)routeNode).getRouteDebugInfo(pathSoFar));
        }
        pathSoFar.pollLast();
        return resolved;
    }

    private static boolean isVariable(String component) {
        return component.equals("*");
    }
}