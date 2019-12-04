package com.cramja.rest.core.route;


public class RouteDebugInfo {

    private final String method;
    private final String path;
    private final Endpoint terminal;

    public RouteDebugInfo(String method, String path, Endpoint terminal) {
        this.method = method;
        this.path = path;
        this.terminal = terminal;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Endpoint getTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        return method + " " + path + " " + terminal.toString();
    }
}
