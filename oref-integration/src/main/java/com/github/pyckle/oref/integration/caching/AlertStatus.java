package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.alerts.details.AlertDetails;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

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
