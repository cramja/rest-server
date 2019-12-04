package com.cramja.rest.core.route;

import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.util.Either;
import java.util.List;
import java.util.Optional;

abstract class AbstractRouteNodeDecorator implements RouteNodeDecorator {

    private RouteNode decoratee;

    public AbstractRouteNodeDecorator() {
    }

    public AbstractRouteNodeDecorator(RouteNode decoratee) {
        this.decoratee = decoratee;
    }

    public final RouteNode getDecoratee() {
        return decoratee;
    }

    public final void setDecoratee(RouteNode decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public String getName() {
        return decoratee.getName();
    }

    @Override
    public void addMethod(Route route, int index, Endpoint endpoint) {
        decoratee.addMethod(route, index, endpoint);
    }

    @Override
    public RouteNode addDecorator(List<String> path, int index, RouteNodeDecorator decorator) {
        this.decoratee = decoratee.addDecorator(path, index, decorator);
        return this;
    }

    @Override
    public HttpResCtx accept(HttpReqCtx req) {
        Either<HttpReqCtx, HttpResCtx> possibleResponse = process(req);
        if (possibleResponse.hasR()) {
            return possibleResponse.right();
        }

        try {
            return decoratee.accept(possibleResponse.left());
        } catch (RuntimeException e) {
            return handleException(e).orElseThrow(() -> e);
        }
    }

    public abstract Either<HttpReqCtx, HttpResCtx> process(HttpReqCtx req);

    public Optional<HttpResCtx> handleException(RuntimeException e) {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return decoratee.toString();
    }

    public String describe() {
        return decoratee.describe();
    }

}
