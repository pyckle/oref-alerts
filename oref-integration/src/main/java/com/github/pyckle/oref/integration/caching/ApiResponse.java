package com.github.pyckle.oref.integration.caching;

import java.net.http.HttpResponse;

public record ApiResponse<T>(
        HttpResponse<?> response,
        T responseObj) {
}
