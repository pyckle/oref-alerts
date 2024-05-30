package com.github.pyckle.oref.integration.activealerts;

import com.github.pyckle.oref.integration.dto.Alert;

import java.time.Instant;
import java.util.List;

public class ActiveAlertFactory {
    /**
     * Build an active alert from an alert
     * @param alert the raw received alert
     * @param displayedAreas the areas that should be displayed from the alert
     * @param receivedTimestamp the timestamp the alert was received
     * @return the built active alert object
     */
    public static ActiveAlert buildActiveAlert(Alert alert, List<String> displayedAreas,
                                               Instant receivedTimestamp) {
        var alertTimestamps = new AlertTimestamps(receivedTimestamp, alert);
        ActiveAlert activeAlert = new ActiveAlert(alertTimestamps, alert, List.copyOf(displayedAreas));
        return activeAlert;
    }
}
