package com.github.pyckle.oref.integration.datetime;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utils for parsing and formatting dates and times returned by Pekudei Oref
 */
public class OrefDateTimeUtils {
    public static final ZoneId ISRAEL_ZONE = ZoneId.of("Asia/Jerusalem");
    private static final DateTimeFormatter shortDateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    /**
     * Format the Instant in local Israel time
     *
     * @param instant the instant to format
     * @return the formatted string
     */
    public static String formatDateAndTimeShort(Instant instant) {
        return shortDateTimeFormatter.format(instant.atZone(ISRAEL_ZONE));
    }

    /**
     * Get the local time in Israel at the specified Instant
     *
     * @param instant the instant to get the local time for
     * @return the local time
     */
    public static LocalTime toLocalTime(Instant instant) {
        return instant.atZone(OrefDateTimeUtils.ISRAEL_ZONE).toLocalTime();
    }
}
