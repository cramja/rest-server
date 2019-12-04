package com.cramja.rest.core.route;

import com.cramja.rest.core.util.Pair;
import java.util.Arrays;
import java.util.Objects;

/**
 * TODO:
 * - validation on components
 */
public class Route {
    private final String method;
    private final String[] normalizedPath;
    private final String[] varNames;

    private Route(
            String method,
            String[] normalizedPath,
            String[] varNames) {
        this.method = method;
        this.normalizedPath = normalizedPath;
        this.varNames = varNames;
    }

    public static Route toRoute(String method, String path) {
        String[] components = path.split("/+");
        String[] normalizedPath = new String[components.length];
        String[] varNames = new String[components.length];

        for (int i = 0; i < components.length; i++) {
            Pair<String, String> pathAndVarName = normalizeComponent(components[i]);
            normalizedPath[i] = pathAndVarName.left();
            varNames[i] = pathAndVarName.right(); // may be null
        }
        return new Route(method.toUpperCase(), normalizedPath, varNames);
    }

    private static Pair<String, String> normalizeComponent(String component) {
        if (component.startsWith("{") && component.endsWith("}")) {
            return Pair.of("*", component.substring(1, component.length() - 1));
        }
        return Pair.ofL(component);
    }

    public String getMethod() {
        return method;
    }

    public String[] getNormalizedPath() {
        return normalizedPath;
    }

    public String getPathVarName(int i) {
        return varNames[i];
    }

    @Override
    public String toString() {
        return method + " " + String.join("/", normalizedPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, Arrays.hashCode(normalizedPath));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Route)) {
            return false;
        }
        Route other = (Route) obj;
        return Objects.equals(other.method, method) &&
                Arrays.equals(other.normalizedPath, normalizedPath);
    }
}