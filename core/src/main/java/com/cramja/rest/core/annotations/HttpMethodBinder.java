package com.cramja.rest.core.annotations;

import static com.cramja.rest.core.route.Route.toRoute;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;

import com.cramja.rest.core.exc.AppConfigException;
import com.cramja.rest.core.exc.BadRequestException;
import com.cramja.rest.core.exc.ServerError;
import com.cramja.rest.core.route.Endpoint;
import com.cramja.rest.core.route.Route;
import com.cramja.rest.core.server.HttpReqCtx;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:
 * - ObjectMapper should be configurable.
 */
public class HttpMethodBinder {
    private static final Logger logger = LoggerFactory.getLogger(HttpMethodBinder.class);

    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    private final HttpServiceBinder serviceBinder;
    private final Method method;

    Route route;
    InvocableMethodEndpoint endpoint;

    HttpMethodBinder(HttpServiceBinder serviceBinder, Method method) {
        this.serviceBinder = serviceBinder;
        this.method = method;
        doExtraction();
    }

    private void doExtraction() {
        if (!isPublic(method.getModifiers())) {
            throw new AppConfigException(
                    format("method %s of service %s was annotated with @Path but was not public",
                            method.getName(), serviceBinder.getService().getClass().getSimpleName())
            );
        }

        Path path = method.getAnnotation(Path.class);
        this.route = toRoute(path.method(), serviceBinder.getBasePath() + path.value());

        // for each argument, check for annotation. Unless the parameter is
        // HttpReqCtx, then it must be annotated so we know how to marshal it
        ArrayList<Function<HttpReqCtx, Object>> extractors = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.getType().equals(HttpReqCtx.class)) {
                extractors.add(ctx -> ctx);
                continue;
            }

            Body body = param.getAnnotation(Body.class);
            PathParam pathParam = param.getAnnotation(PathParam.class);
            QueryParam queryParam = param.getAnnotation(QueryParam.class);
            AttributeParam attrParam = param.getAnnotation(AttributeParam.class);

            int annoCount =
                    (body == null ? 0 : 1) +
                    (pathParam == null ? 0 : 1) +
                    (queryParam == null ? 0 : 1) +
                    (attrParam == null ? 0 : 1);

            if (annoCount == 0) {
                throw new AppConfigException(
                        format("parameter %d of method %s was not annotated", i, method.getName()));
            }
            if (annoCount > 1) {
                throw new AppConfigException(
                        format("parameter %d of method %s was annotated more than once", i, method.getName()));
            }

            if (body != null) {
                extractors.add(deserializeBody(param.getType()));
                continue;
            } else if (pathParam != null) {
                extractors.add(getPathParam(pathParam.value(), param.getType()));
                continue;
            } else if (queryParam != null) {
                extractors.add(getQueryParam(queryParam.value(), param.getType()));
                continue;
            } else if (attrParam != null) {
                extractors.add(getAttributeParam(attrParam.value(), param.getType()));
                continue;
            }
            throw new AssertionError("line unreachable");
        }

        int code = 200;
        ResponseCode codeAnno = method.getAnnotation(ResponseCode.class);
        if (codeAnno != null) {
            code = codeAnno.value();
        }

        this.endpoint = new InvocableMethodEndpoint(serviceBinder.getService(), method, extractors, code);
    }

    private Function<HttpReqCtx, Object> getQueryParam(String queryParamName, Class<?> type) {
        return (ctx) -> {
            List<String> params = ctx.getQueryParams().get(queryParamName);
            if (params == null || params.isEmpty()) {
                checkParamNotNull("query param '" + queryParamName + "'", null);
            }
            return unmarshal(queryParamName, type, params.get(0));
        };
    }

    private Function<HttpReqCtx, Object> getPathParam(String pathParamName, Class<?> type) {
        return (ctx) -> {
            String param = ctx.getPathParams().get(pathParamName);
            return unmarshal(pathParamName, type, param);
        };
    }

    private Function<HttpReqCtx, Object> getAttributeParam(String paramName, Class<?> type) {
        return (ctx) -> {
            Object param = ctx.getAttributes().get(paramName);
            if (param == null) {
                return null;
            }

            if (type.isAssignableFrom(param.getClass())) {
                return param;
            }

            logger.warn("attributeParam {} was type {} but expected type {}. returning null",
                    paramName, param.getClass(), type);
            return null;
        };
    }

    private Object unmarshal(String paramName, Class<?> paramType, String param) {
        checkParamNotNull("param '" + paramName + "'", param);
        try {
            if (paramType == String.class) {
                return param;
            } else if (paramType == Long.class || paramType == long.class) {
                return Long.parseLong(param);
            } else if (paramType == Integer.class || paramType == int.class) {
                return Integer.parseInt(param);
            } else if (paramType == UUID.class) {
                return UUID.fromString(param);
            } else {
                throw new ServerError("cannot unmarshal type " + paramType);
            }
        } catch (RuntimeException e) {
            throw new BadRequestException(
                    "unable to parse param " + paramName + " as " + paramType.getSimpleName());
        }
    }

    private static void checkParamNotNull(String paramDesc, Object param) {
        if (param == null) {
            throw new BadRequestException(paramDesc + " was not present in request");
        }
    }

    private Function<HttpReqCtx, Object> deserializeBody(Class<?> type) {
        return (ctx) -> {
            try {
                return mapper.readValue(ctx.getBody(), type);
            } catch (UnrecognizedPropertyException e) {
                throw new BadRequestException(
                        String.format("Unrecognized property '%s'. Must be one of %s.",
                                e.getPropertyName(),
                                e.getKnownPropertyIds()),
                        e);
            } catch (IOException e) {
                throw new BadRequestException("unable to parse request body", e);
            }
        };
    }

    public Route getRoute() {
        return route;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
}
