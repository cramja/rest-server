package com.cramja.rest.core.route;

public interface RouteNodeDecorator extends RouteNode {

    RouteNode getDecoratee();

    void setDecoratee(RouteNode decoratee);

}
