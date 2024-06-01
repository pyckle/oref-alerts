package com.github.pyckle.oref.integration.caching;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachedApiResultTest {
    @Test
    void testMaxAgeParsing() {
        Map<String, Long> strToExpected = Map.of(
                "max-age=3", 3L,
                "max-age=4", 4L,
                "max-age=30", 30L,
                "private, max-age=3", 3L,
                "private,max-age=3", 3L,
                "max-age=3, public", 3L,
                "max-age=3,public", 3L,
                "must-revalidate,max-age=3,public", 3L,
                "max-age=000003", 3L
        );
        for (var testVals : strToExpected.entrySet()) {
            String key = testVals.getKey();
            long actual = CachedApiResult.maxAge(Optional.ofNullable(key));
            Long expected = testVals.getValue();
            assertEquals(expected, actual);
        }
        assertEquals(-1L, CachedApiResult.maxAge(Optional.empty()));
    }
}
