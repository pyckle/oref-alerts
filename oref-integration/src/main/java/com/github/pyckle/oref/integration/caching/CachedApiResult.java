package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.integration.translationstores.DistrictStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public record CachedApiResult<T>(Instant localTimestamp,
                                 int statusCode,
                                 Instant serverTimestamp,
                                 Instant lastModified,
                                 long maxAge,
                                 T retrievedValue) {
    // a default value for initialization to avoid ugly Null Pointer checks.
    private static final CachedApiResult<?> UNINITIALIZED_RESULT =
            new CachedApiResult<>(Instant.EPOCH, -1, Instant.EPOCH, Instant.EPOCH, 0, null);

    private static final Logger logger = LoggerFactory.getLogger(DistrictStore.class);
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String SERVER_TIMESTAMP = "Date";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final Pattern MAX_AGE_PATTERN = Pattern.compile("max-age=(\\d{1,18})");

    @SuppressWarnings("unchecked")
    public static <T> CachedApiResult<T> getUninitializedResult() {
        return (CachedApiResult<T>) UNINITIALIZED_RESULT;
    }

    public static <T> CachedApiResult<T> buildCachedApiResult(Instant localTime,
                                                              HttpResponse<?> response,
                                                              T retrievedValue) {

        int statusCode = response.statusCode();
        Instant serverTimestamp = parseDate(response.headers(), SERVER_TIMESTAMP);
        Instant lastModified = parseDate(response.headers(), LAST_MODIFIED_HEADER);
        long maxAge = maxAge(response.headers());

        return new CachedApiResult<>(localTime, statusCode, serverTimestamp, lastModified, maxAge, retrievedValue);
    }

    static long maxAge(HttpHeaders headers) {
        var header = headers.firstValue(CACHE_CONTROL);
        return maxAge(header);
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


    private static Instant parseDate(HttpHeaders headers, String header) {
        var headerDateStr = headers.firstValue(header);
        if (headerDateStr.isPresent()) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(headerDateStr.get(), DateTimeFormatter.RFC_1123_DATE_TIME);
                return zdt.toInstant();
            } catch (DateTimeParseException ex) {
                logger.debug("Could not parse date: {}", headerDateStr.get());
                logger.trace("Could not parse date", ex);
            }
        }
        return null;
    }

    public Instant getLastUpdated() {
        // prefer the last modified time if present, if not, server timestamp, otherwise local time.
        return Objects.requireNonNullElse(this.lastModified(),
                Objects.requireNonNullElse(this.serverTimestamp(), this.localTimestamp()));
    }
}
