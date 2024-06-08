package com.github.pyckle.oref.integration.caching;

public record UpdateResult<T>(boolean success, CachedApiCall<T> nextCallToTrigger) {
    public boolean hasNextCallToTrigger() {
        return nextCallToTrigger() != null;
    }
}
