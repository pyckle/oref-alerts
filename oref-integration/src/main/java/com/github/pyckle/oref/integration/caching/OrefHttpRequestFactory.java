package com.github.pyckle.oref.integration.caching;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

public class OrefHttpRequestFactory {
    // timeout is set to 8 seconds in javascript in Oref Website
    private static final Duration callTimeout = Duration.ofMillis(8_000);

    /**
     * Build a http GET request for the specified url
     *
     * @param uri     the uri to GET
     * @param headers the additional headers to include. MUST be an even number
     * @return the built request
     */
    static HttpRequest buildRequest(URI uri, String... headers) {
        var ret = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(callTimeout);
        if (headers.length > 0) {
            ret.headers(headers);
        }
        return ret.build();
    }
}
