package com.github.pyckle.oref.integration.caching;

import okhttp3.Response;

public record ApiResponse<T>(
        Response response,
        T responseObj) {
}
