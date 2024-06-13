package com.github.pyckle.oref.alerts.details;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A class which can describe an alert received from any of Oref's REST APIs
 *
 * @param alertSource        the source API of the alert
 * @param receivedTimestamp  the timestamp when this alert was received
 * @param remoteTimestamp    the timestamp the server specified for this alert. May fallback to local time if parsing fails
 * @param isDrill            whether this alert is a drill. See {@link com.github.pyckle.oref.alerts.categories.AlertCategories#isDrill(String)}
 * @param category           the category of this alert in Hebrew
 * @param translatedCategory the category of this alert translated to the local language
 * @param locations          the translated alerted areas
 * @param locationsHeb       the alerted areas in Hebrew
 */
public record AlertDetails(
        AlertSource alertSource,
        Instant receivedTimestamp,
        LocalDateTime remoteTimestamp,
        boolean isDrill,
        String category,
        String translatedCategory,
        List<String> locations,
        List<String> locationsHeb
) {
}
