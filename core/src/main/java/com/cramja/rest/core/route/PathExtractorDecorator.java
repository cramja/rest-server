package com.cramja.rest.core.route;

import com.cramja.rest.core.exc.ServerError;
import com.cramja.rest.core.server.HttpReqCtx;
import com.cramja.rest.core.server.HttpResCtx;
import com.cramja.rest.core.util.Either;

class PathExtractorDecorator extends AbstractRouteNodeDecorator {

    private final String varName;
    private final int pathIndex;

    public PathExtractorDecorator(
            RouteNode decoratee,
            String varName,
            int pathIndex) {
        super(decoratee);
        this.varName = varName;
        this.pathIndex = pathIndex;
    }

    @Override
    public Either<HttpReqCtx, HttpResCtx> process(HttpReqCtx req) {
        if (pathIndex < req.getPath().size()) {
            String var = req.getPath().get(pathIndex);
            req.getPathParams().put(varName, var);
        } else {
            throw new ServerError("unable to extract path variable " + varName);
        }
        return Either.ofL(req);
    }
}
