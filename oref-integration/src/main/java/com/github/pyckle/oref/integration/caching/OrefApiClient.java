package com.github.pyckle.oref.integration.caching;

import com.google.common.base.Stopwatch;
import com.google.common.io.CountingInputStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class OrefApiClient {
    private static final Logger logger = LoggerFactory.getLogger(OrefApiClient.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int SUCCESS_STATUS_CODE = 200;

    public static <T> ApiResponse<T> get(HttpRequest req, TypeToken<T> typeToken)
            throws IOException, InterruptedException {
        Gson gson = new Gson();

        Stopwatch stopwatch = Stopwatch.createStarted();
        HttpResponse<InputStream> httpResponse = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        // Body stream *MUST* be listed separately because we always want to close it even with there is an error
        // creating the gzipReader. Not closing the body stream is a memory leak..
        try (CountingInputStream is = new CountingInputStream(httpResponse.body());
             InputStream gzipReader = wrapWithGzip(httpResponse, is);
             InputStreamReader isr = new InputStreamReader(gzipReader, StandardCharsets.UTF_8)) {
            if (httpResponse.statusCode() != SUCCESS_STATUS_CODE) {
                logger.warn("Failed http request: {} {} {}", req.uri(), httpResponse.statusCode(), httpResponse.headers());
                throw new RuntimeException("Unexpected Status Code " + httpResponse.statusCode());
            } else {
                T ret = gson.fromJson(isr, typeToken);

                logger.debug("Fetched {} bytes in Request to {}: Response: {} in {}", is.getCount(), req.uri(),
                        httpResponse.statusCode(), stopwatch.elapsed());
                logger.trace("Request to {}: Response: {} {} {}", req, httpResponse.statusCode(),
                        httpResponse.headers(), ret);

                return new ApiResponse<>(httpResponse, ret);
            }
        }
    }

    private static InputStream wrapWithGzip(HttpResponse<InputStream> httpResponse, InputStream is) throws IOException {
        var contentEncoding = httpResponse.headers().allValues("Content-Encoding");
        if (!contentEncoding.isEmpty()) {
            if (contentEncoding.size() == 1 && contentEncoding.get(0).equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            } else {
                throw new IllegalStateException("Unknown content encoding " + contentEncoding);
            }
        }
        return is;
    }
}
