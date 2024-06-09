package com.github.pyckle.oref.integration.caching;

public record UpdateResult(boolean success, CachedApiCall<?> nextCallToTrigger) {
    public boolean hasNextCallToTrigger() {
        return nextCallToTrigger() != null;
    }
}
