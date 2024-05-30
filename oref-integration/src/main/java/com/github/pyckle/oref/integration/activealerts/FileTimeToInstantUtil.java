package com.github.pyckle.oref.integration.activealerts;

import java.time.Instant;

/**
 * Converts file time to Instant
 */
public class FileTimeToInstantUtil {
    private static final long FILETIME_EPOCH_DIFF = 11644473600L;


    public static Instant fileTimeToInstant(String fileTime) {
        if (fileTime == null || fileTime.length() < 8) {
            // malformed
            return Instant.EPOCH;
        }
        try {
            var ret = Instant.ofEpochSecond(getEpochSecond(fileTime), getNanos(fileTime));

            // if we get something before the epoch, something went wrong.
            if (Instant.EPOCH.isAfter(ret))
                return Instant.EPOCH;

            return ret;
        } catch (NumberFormatException e) {
            return Instant.EPOCH;
        }
    }

    private static long getEpochSecond(String alertId) {
        String secondsFromMicrosoftEpoch = alertId.substring(0, alertId.length() - 7);
        long seconds = Long.parseLong(secondsFromMicrosoftEpoch) - FILETIME_EPOCH_DIFF;
        return seconds;
    }

    private static long getNanos(String alertId) {
        String nanosSubstr = alertId.substring(alertId.length() - 7);
        long nanoOffset = Long.parseLong(nanosSubstr);
        return nanoOffset;
    }
}
