package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.alerts.details.AlertDetails;
import com.github.pyckle.oref.integration.translationstores.DistrictStore;
import com.github.pyckle.oref.integration.translationstores.UpdateFlashType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AlertStatus {
    private final List<AlertDetails> alerts;
    private final Instant lastUpdate;

    public AlertStatus(List<AlertDetails> alerts, Instant lastUpdate) {
        this.alerts = alerts;
        this.lastUpdate = lastUpdate;
    }

    public List<AlertDetails> getAlerts() {
        return alerts;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public ActiveAlertState activeAlertState(LocalDateTime threshold) {
        return activeAlertState(threshold, null);
    }

    public ActiveAlertState activeAlertState(LocalDateTime threshold, String alertAreaHeb)
    {
        boolean hasAlert = false;
        UpdateFlashType mostSevereUpdateColor = null;
        var matchingAlerts = alertAreaHeb == null ? null : Set.of(alertAreaHeb);
        for (var alert : getAlerts())
        {
            if (alert.remoteTimestamp().isAfter(threshold)) {
                break;
            }
            if (alertMatches(alert, matchingAlerts)) {
                if (alert.isUpdateOrFlash()) {
                    if (mostSevereUpdateColor == null || alert.updateFlashType().getSeverity() < mostSevereUpdateColor.getSeverity())
                        mostSevereUpdateColor = alert.updateFlashType();
                } else {
                    // alerts take priority over flashes
                    hasAlert = true;
                }
            }
        }
        return new ActiveAlertState(hasAlert, mostSevereUpdateColor);
    }

    private static boolean alertMatches(AlertDetails alert, Set<String> matchingAlerts)
    {
        if (matchingAlerts == null)
            return true;
        for (var loc : alert.locationsHeb()) {
            if (DistrictStore.COUNTRYWIDE_ALERT.contains(loc) || matchingAlerts.contains(loc))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertStatus that = (AlertStatus) o;
        return Objects.equals(alerts, that.alerts) && Objects.equals(lastUpdate, that.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alerts, lastUpdate);
    }
}
