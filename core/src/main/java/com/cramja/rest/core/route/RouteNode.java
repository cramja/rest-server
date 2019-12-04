package com.cramja.rest.core.route;

import java.util.List;

interface RouteNode extends Endpoint {

    String getName();

    void addMethod(Route route, int index, Endpoint endpoint);

    RouteNode addDecorator(List<String> path, int index, RouteNodeDecorator decorator);

    String describe();

}
