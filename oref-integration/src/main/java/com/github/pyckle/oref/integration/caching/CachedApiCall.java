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

/**
 * Represents an API call where the return value can (or should) be cached.
 *
 * @param <T> the processed cached result of the API call
 */
public class CachedApiCall<T> {
    private static final Logger logger = LoggerFactory.getLogger(CachedApiCall.class);

    private final HttpRequest req;
    private final Duration cacheDuration;
    private final Duration waitOnFailure;
    private final Callable<ApiResponse<T>> updateCallback;
    private final CachedApiResult<T> defaultValue;
    private volatile CachedApiResult<T> cachedValue = null;


    /**
     * Construct a new element that caches Oref services
     *
     * @param req           The req to send
     * @param cacheDuration How long after fetching the url should we fetch it again
     * @param waitOnFailure If the request fails, how long to wait before attempting the request again
     */
    public CachedApiCall(HttpRequest req, Duration cacheDuration, Duration waitOnFailure, TypeToken<T> typeToken, T defaultValue) {
        this(req, cacheDuration, waitOnFailure, typeToken, defaultValue, ApiResponse::responseObj);
    }

    public <U> CachedApiCall(HttpRequest req,
                             Duration cacheDuration,
                             Duration waitOnFailure,
                             TypeToken<U> typeToken,
                             T defaultValue,
                             Function<ApiResponse<U>, T> transformer) {
        this.req = req;
        this.cacheDuration = cacheDuration;
        this.waitOnFailure = waitOnFailure;
        this.updateCallback = () -> {
            var apiResp = OrefApiClient.get(req, typeToken);
            return new ApiResponse<>(apiResp.response(), transformer.apply(apiResp));
        };
        this.cachedValue = this.defaultValue = new CachedApiResult<>(Instant.EPOCH, -1, Instant.EPOCH, Instant.EPOCH, 0, defaultValue);
    }

    public CachedApiResult<T> getCachedValue() {
        return Objects.requireNonNullElse(cachedValue, defaultValue);
    }

    public Duration getCacheDuration() {
        return cacheDuration;
    }

    public boolean isInitializedYet() {
        return cachedValue != null;
    }

    public UpdateResult<T> update() throws InterruptedException {
        long start = System.nanoTime();
        try {
            var res = updateCallback.call();
            var lastUpdate = Instant.now();
            this.cachedValue = CachedApiResult.buildCachedApiResult(lastUpdate, res.response(), res.responseObj());
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            // try again later
            logger.warn("Failure to fetch update {}", ex.getLocalizedMessage());
            logger.debug("Failure to fetch update", ex);
            return new UpdateResult<>(false, null);
        }
        logger.debug("Fetched {} in {}ns", req, System.nanoTime() - start);
        return new UpdateResult<>(true, null);
    }

    public Duration getWaitOnFailure() {
        return waitOnFailure;
    }
}
