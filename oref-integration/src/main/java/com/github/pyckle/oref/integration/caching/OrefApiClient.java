package com.github.pyckle.oref.integration.caching;

import com.google.common.base.Stopwatch;
import com.google.common.io.CountingInputStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.CacheControl;
import okhttp3.ConnectionPool;
import okhttp3.Dns;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrefApiClient {
    private static final Logger logger = LoggerFactory.getLogger(OrefApiClient.class);
    private static OkHttpClient client = null;
    private static ConnectionPool connectionPool;
    private static final int SUCCESS_STATUS_CODE = 200;

    public static <T> ApiResponse<T> get(HttpRequest req, TypeToken<T> typeToken)
            throws IOException, InterruptedException {
        Gson gson = new Gson();

        Stopwatch stopwatch = Stopwatch.createStarted();

        List<String> allheaders = convertHeadersToOkHttp(req);
        var okHttpReq = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noCache().build())
                .headers(Headers.of(allheaders.toArray(new String[0])))
                .url(req.uri().toURL())
                .build();
        var httpResponse = getHttpClient().newCall(okHttpReq).execute();
        logger.debug("Received resp - content-length {} bytes in Request to {}: Response: {} in {}",
                httpResponse.body() == null ? -1 : httpResponse.body().contentLength(), req.uri(),
                httpResponse.code(), stopwatch.elapsed());

        try (var is = httpResponse.body().byteStream();
             var wrappedReader = new CountingInputStream(wrapWithGzip(httpResponse.request().url(), httpResponse.headers(), is));
             var isr = new BufferedReader(new InputStreamReader(wrappedReader, StandardCharsets.UTF_8))) {
            if (httpResponse.code() != SUCCESS_STATUS_CODE) {
                logger.warn("Failed http request: {} {} {}", req.uri(), httpResponse.code(),
                        httpResponse.headers());
                throw new RuntimeException("Unexpected Status Code " + httpResponse.code());
            } else {
                T ret = gson.fromJson(isr, typeToken);

                logger.debug("Decoded {} bytes uncompressed: {} bytes in Request to {}: Response: {} in {}",
                        httpResponse.body().contentLength(), wrappedReader.getCount(), req.uri(), httpResponse.code(),
                        stopwatch.elapsed());
                logger.trace("Request to {}: Response: {} {} {}", req, httpResponse.code(),
                        httpResponse.headers(), ret);

                return new ApiResponse<>(httpResponse, ret);
            }
        } catch (Exception ex) {
            logger.info("Failed to decode resp {}", httpResponse.request().url());
            // evicting from the pool forces a reconnect, hopefully to a different host that is not broken.
            connectionPool.evictAll();
            throw ex;
        }
    }

    @NotNull
    private static List<String> convertHeadersToOkHttp(HttpRequest req)
    {
        List<String> allheaders = new ArrayList<>();
        for (var h : req.headers().map().entrySet())
        {
            for (var val : h.getValue())
            {
                allheaders.add(h.getKey());
                allheaders.add(val);
            }
        }
        return allheaders;
    }

    private static synchronized OkHttpClient getHttpClient() {
        if (client == null)
        {
            connectionPool = new ConnectionPool();
            client = new OkHttpClient.Builder()
                    .callTimeout(Duration.ofSeconds(8))
                    .connectionPool(connectionPool)
                    .dns(new RotatingDns())
                    .build();
        }
        return client;
    }

    private static InputStream wrapWithGzip(HttpUrl url,  Headers responseHeaders, InputStream is) throws IOException {
        var contentEncoding = responseHeaders.toMultimap().get("Content-Encoding");
        if (!contentEncoding.isEmpty()) {
            if (contentEncoding.size() == 1 && contentEncoding.get(0).equalsIgnoreCase("gzip")) {
                logger.debug("response is gzipped encoded {}", url);
                return new GzipCompressorInputStream(is);
            } else {
                throw new IllegalStateException("Unknown content encoding " + contentEncoding);
            }
        }
        logger.debug("response is not gzipped encoded {}", url);
        return is;
    }

    /**
     * A dns resolver that ensures that the returned hosts are shuffled and the same host is not returned at the start of the list twice
     */
    private static class RotatingDns implements Dns
    {
        private final Map<String, InetAddress> cachedIps = new HashMap<>();

        @NotNull
        @Override
        public synchronized List<InetAddress> lookup(@NotNull String hostname) throws UnknownHostException
        {
            try
            {
                var ips = InetAddress.getAllByName(hostname);
                if (ips.length > 1 && cachedIps.containsKey(hostname))
                {
                    var oldFirstAddr = cachedIps.get(hostname);
                    Collections.shuffle(Arrays.asList(ips));

                    // make sure that the last first IP we returned is not the first IP again, since that is likely what was used.
                    if (oldFirstAddr.equals(ips[0]))
                    {
                        ips[0] = ips[ips.length - 1];
                        ips[ips.length - 1] = oldFirstAddr;
                    }
                }
                if (ips.length > 0)
                {
                    cachedIps.put(hostname, ips[0]);
                }
                var ret = List.of(ips);
                logger.info("resolved {} to {}", hostname, ret);
                return ret;
            }
            catch (NullPointerException e)
            {
                throw new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
            }
        }
    }
}
