package com.github.pyckle.oref.integration.dto;

/**
 * <a href="https://www.oref.org.il/warningMessages/History/AlertsHistory.json">The AlertsHistory</a> API returns a list of this DTO
 *
 * @param alertDate the date of the alert. Example: "2024-06-08 17:29:34". Parse with {@link com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils#parseAlertHistoryTimestamp(String)}
 * @param title     the category of this alert in Hebrew
 * @param data      the data in this alert
 * @param category  the int category of this alert corresponding to {@link Category#id()} and {@link LeftoverAlertDescription#category()}
 */
public record AlertHistory(
        String alertDate,
        String title,
        String data,
        int category) {
}
