package com.cramja.rest.core.server;

import io.netty.handler.codec.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HttpReqCtx {

    String method;
    List<String> path;
    @Builder.Default Map<String, String> pathParams = new HashMap<>();
    Map<String, List<String>> queryParams;
    HttpHeaders headers;
    String body;

    /**
     * Metadata derived from the request. Might include signed session id,
     * authenticated username, etc.
     */
    @Builder.Default Map<String,Object> attributes = new HashMap<>();

    public String getBody() {
        return body;
    }

}
