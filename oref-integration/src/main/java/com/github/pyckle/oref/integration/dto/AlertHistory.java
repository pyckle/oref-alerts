package com.github.pyckle.oref.integration.dto;

/**
 * <a href="https://www.oref.org.il/WarningMessages/History/AlertsHistory.json">The AlertsHistory</a> API returns a list of this DTO
 *
 * @param alertDate the date of the alert
 * @param title     the category of this alert in Hebrew
 * @param data      the data in this alert
 * @param category  the int category of this alert corresponding to {@link Category#id()}
 */
public record AlertHistory(
        String alertDate,
        String title,
        String data,
        int category) {
}
