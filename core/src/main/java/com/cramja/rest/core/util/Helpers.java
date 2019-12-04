package com.cramja.rest.core.util;

import com.cramja.rest.core.exc.ServerError;

public class Helpers {

    public static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new ServerError(message);
        }
    }

    public static String canonicalPath(String path) {
        if (path.equals("/")) {
            return "";
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

}
