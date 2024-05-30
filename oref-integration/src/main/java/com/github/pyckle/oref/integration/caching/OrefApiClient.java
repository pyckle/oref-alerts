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

    public static <T> T get(HttpRequest req, TypeToken<T> typeToken) throws IOException, InterruptedException {
        Gson gson = new Gson();
        InputStream is;

        HttpResponse<InputStream> responseIs = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        is = responseIs.body();
        T ret = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), typeToken);

        if (ret == null) {
            logger.debug("Null Body: {} {} {}", req, responseIs.statusCode(), responseIs.headers());
        }

        return ret;
    }

}
