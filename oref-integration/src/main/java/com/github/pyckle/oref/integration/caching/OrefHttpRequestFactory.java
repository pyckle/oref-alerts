package com.github.pyckle.oref.integration.caching;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Arrays;

public class OrefHttpRequestFactory {
    // timeout is set to 8 seconds in javascript in Oref Website. Increased due to slow api calls
    private static final Duration callTimeout = Duration.ofMillis(12_000);
    private static final String acceptEncodingHeader = "Accept-Encoding";
    private static final String acceptEncodingGzip = "gzip";

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
        String[] allHeaders = addAcceptEncodingGzip(headers);

        if (allHeaders.length > 1)
            ret.headers(allHeaders);
        return ret.build();
    }

    private static String[] addAcceptEncodingGzip(String[] headers) {
        String[] allHeaders = addHeader(headers, acceptEncodingHeader, acceptEncodingGzip);
        return allHeaders;
    }

    private static String[] addHeader(String[] headers, String headerToAdd, String headerVal)
    {
        String[] allHeaders = null;
        for (int i = 0; i < headers.length; i += 2) {
            if (headers[i].equalsIgnoreCase(headerToAdd)) {
                allHeaders = headers;
                break;
            }
        }
        if (allHeaders == null) {
            allHeaders = Arrays.copyOf(headers, headers.length + 2);
            allHeaders[allHeaders.length - 2] = headerToAdd;
            allHeaders[allHeaders.length - 1] = headerVal;
        }
        return allHeaders;
    }
}
