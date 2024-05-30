package com.github.pyckle.oref.integration.caching;

import java.time.Instant;

public record CachedApiResult<T>(Instant lastRetrieved, T retrievedValue) {
    // a default value for initialization to avoid ugly Null Pointer checks.
    private static final CachedApiResult<?> UNINITIALIZED_RESULT = new CachedApiResult<>(Instant.EPOCH, null);

    @SuppressWarnings("unchecked")
    public static <T> CachedApiResult<T> getUninitializedResult() {
        return (CachedApiResult<T>) UNINITIALIZED_RESULT;
    }
}
