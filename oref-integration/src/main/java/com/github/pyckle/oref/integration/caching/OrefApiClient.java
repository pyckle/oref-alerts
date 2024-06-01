package com.github.pyckle.oref.integration.caching;

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

public class OrefApiClient {
    private static final Logger logger = LoggerFactory.getLogger(OrefApiClient.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int SUCCESS_STATUS_CODE = 200;

    public static <T> ApiResponse<T> get(HttpRequest req, TypeToken<T> typeToken)
            throws IOException, InterruptedException {
        Gson gson = new Gson();
        InputStream is;

        HttpResponse<InputStream> httpResponse = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (httpResponse.statusCode() != SUCCESS_STATUS_CODE) {
            logger.warn("Failed http request: {} {} {}", req.uri(), httpResponse.statusCode(), httpResponse.headers());
            throw new RuntimeException("Unexpected Status Code " + httpResponse.statusCode());
        } else {
            is = httpResponse.body();
            T ret = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), typeToken);

            logger.debug("Request to {}: Response: {} {} {}", req, httpResponse.statusCode(), httpResponse.headers(), ret);

            return new ApiResponse<>(httpResponse, ret);
        }
    }

}
