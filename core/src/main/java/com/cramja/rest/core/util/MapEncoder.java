package com.cramja.rest.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

// TODO: malformed input
public final class MapEncoder {
    private MapEncoder() {}

    public static Map<String, String> decode(String encoded) {
        if (encoded.length() == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> vals = new HashMap<>();
        final String[] pairs = encoded.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            vals.put(dec(kv[0]), dec(kv[1]));
        }
        return vals;
    }

    public static String encode(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Entry<String,String> entry : params.entrySet()) {
            sb.append(enc(entry.getKey()));
            sb.append("=");
            sb.append(enc(entry.getValue()));
            sb.append("&");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static String enc(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String dec(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
