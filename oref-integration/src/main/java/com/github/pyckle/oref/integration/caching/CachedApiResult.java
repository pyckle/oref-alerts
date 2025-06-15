package com.github.pyckle.oref.integration.caching;

import okhttp3.Headers;
import okhttp3.Response;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public record CachedApiResult<T>(Instant localTimestamp,
                                 int statusCode,
                                 Instant serverTimestamp,
                                 Instant lastModified,
                                 long maxAge,
                                 T retrievedValue) {

    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String SERVER_TIMESTAMP = "Date";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final Pattern MAX_AGE_PATTERN = Pattern.compile("max-age=(\\d{1,18})");

    public static <T> CachedApiResult<T> buildCachedApiResult(Instant localTime,
                                                              Response response,
                                                              T retrievedValue) {

        int statusCode = response.code();
        Instant serverTimestamp = parseDate(response.headers(), SERVER_TIMESTAMP);
        Instant lastModified = parseDate(response.headers(), LAST_MODIFIED_HEADER);
        long maxAge = maxAge(response.headers());

        return new CachedApiResult<>(localTime, statusCode, serverTimestamp, lastModified, maxAge, retrievedValue);
    }

    static long maxAge(Headers headers) {
        var header = headers.get(CACHE_CONTROL);
        return maxAge(Optional.ofNullable(header));
    }

    static long maxAge(Optional<String> header) {
        if (header.isPresent()) {
            var matcher = MAX_AGE_PATTERN.matcher(header.get());
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        }
        return -1L;
    }


    private static Instant parseDate(Headers headers, String header) {
        Date date = headers.getDate(header);
        return date == null ? null : date.toInstant();
    }

    /**
     * Get the last time this API was updated. This call goes in order of preference of what is available:
     * <pre>
     * 1) Last Modified HTTP header.
     * 2) Remote Server Time
     * 3) Time last response was received
     * </pre>
     *
     * @return last updated time
     */
    public Instant getLastUpdated() {
        // prefer the last modified time if present, if not, server timestamp, otherwise local time.
        return Objects.requireNonNullElse(this.lastModified(), getLastFetched());
    }


    /**
     * Get the last time this API was fetched.
     * <pre>
     * 1) Remote Server Time
     * 2) Time last response was received
     * </pre>
     *
     * @return
     */
    public Instant getLastFetched() {
        return Objects.requireNonNullElse(this.serverTimestamp(), this.localTimestamp());
    }
}
