package com.github.pyckle.oref.integration.caching;

import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class CachedApiCall<T> {
    private static final Logger logger = LoggerFactory.getLogger(CachedApiCall.class);

    private final HttpRequest req;
    private final Duration cacheDuration;
    private final Duration waitOnFailure;
    private final Callable<T> updateCallback;
    private volatile CachedApiResult<T> cachedValue = null;


    /**
     * Construct a new element that caches Oref services
     *
     * @param req           The req to send
     * @param cacheDuration How long after fetching the url should we fetch it again
     * @param waitOnFailure If the request fails, how long to wait before attempting the request again
     */
    public CachedApiCall(HttpRequest req, Duration cacheDuration, Duration waitOnFailure, TypeToken<T> typeToken) {
        this(req, cacheDuration, waitOnFailure, typeToken, Function.identity());
    }

    public <U> CachedApiCall(HttpRequest req,
                             Duration cacheDuration,
                             Duration waitOnFailure,
                             TypeToken<U> typeToken,
                             Function<U, T> transformer) {
        this.req = req;
        this.cacheDuration = cacheDuration;
        this.waitOnFailure = waitOnFailure;
        this.updateCallback = () -> {
            U apiResp = OrefApiClient.get(req, typeToken);
            return transformer.apply(apiResp);
        };
    }

    public CachedApiResult<T> getCachedValue() {
        return Objects.requireNonNullElse(cachedValue, CachedApiResult.getUninitializedResult());
    }

    public Duration getCacheDuration() {
        return cacheDuration;
    }

    public boolean isInitializedYet() {
        return cachedValue != null;
    }

    public boolean update() throws InterruptedException {
        try {
            T res = updateCallback.call();
            var lastUpdate = Instant.now();
            this.cachedValue = new CachedApiResult<>(lastUpdate, res);
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            // try again later
            logger.error("Failure to fetch update {}", ex.getLocalizedMessage());
            logger.debug("Failure to fetch update", ex);
            return false;
        }
        logger.debug("Fetched {}", req);
        return true;
    }

    public Duration getWaitOnFailure() {
        return waitOnFailure;
    }
}
