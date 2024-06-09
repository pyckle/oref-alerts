package com.github.pyckle.oref.integration.activealerts;

import com.github.pyckle.oref.integration.caching.ApiResponse;
import com.github.pyckle.oref.integration.dto.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A store for arriving alerts that rolls over after some duration. This allows alerts to displayed longer during an
 * active alert events
 */
public class AlertsRolloverStorage {
    private static final Logger logger = LoggerFactory.getLogger(AlertsRolloverStorage.class);

    /**
     * The max length of time to overlap with the alert history
     */
    static final Duration overlapWithAlertHistory = Duration.ofMinutes(3);

    /**
     * The amount of time that must pass before an area is displayed again due to being present in multiple alerts. Need
     * to do some analysis on historical alerts to determine the most proper value. It may be that the alerts API doesn't
     * give us enough info to deterministically understand whether an alert actually is duplicated, and we have to make
     * a best effort.
     */
    static final Duration repeatCityThreshold = Duration.ofSeconds(50);

    private final Deque<ActiveAlert> rollingAlerts;

    // a linked hashmap to track which areas were recently alerted.
    private final LinkedHashMap<AlertedAreaWithCategory, Instant> rollingAlertDupePreventor = new LinkedHashMap<>();

    private final Supplier<Instant> lastAlertHistoryUpdate;

    /**
     * The computed active alerts. Must *always* be immutible
     */
    private List<ActiveAlert> activeAlerts = List.of();

    /**
     * The last received alert. This is only updated if a valid alert is received, not if null is received (although it
     * is initialized to null). This is used in the event of Pekudei Oref replaying an old alert after a long period
     * of time, as recently occurred.
     */
    private Alert lastReceivedAlert;

    public AlertsRolloverStorage() {
        this(Instant::now);
    }

    public AlertsRolloverStorage(Supplier<Instant> lastAlertHistoryUpdate) {
        this.lastAlertHistoryUpdate = lastAlertHistoryUpdate;
        this.rollingAlerts = new ArrayDeque<>();
    }

    public List<ActiveAlert> addAlert(ApiResponse<Alert> alert) {
        return this.addAlert(alert, Instant.now(), lastAlertHistoryUpdate.get());
    }

    /**
     * @param alert                  the alert to add
     * @param now                    The current time - used to avoid duplicate alerts for the same area
     * @param lastAlertHistoryUpdate the last time the alert history was updated. Used to clean stale events that have
     *                               already been received by the history service.
     * @return the current list of alerts
     */
    public List<ActiveAlert> addAlert(ApiResponse<Alert> alert, Instant now, Instant lastAlertHistoryUpdate) {
        boolean mutatedAlerts = false;

        // Remove old alerts first in the unlikely case that Pekudei Oref is sending the same alert for over the
        // rollover duration. Probably never occurs, but better to read and display in this strange case.
        mutatedAlerts |= removeOldAlerts(lastAlertHistoryUpdate);

        // note that this does not change whether we mutated alerts.
        cleanupRepeatPreventorMap(now);

        mutatedAlerts |= addAlertIfNew(alert.responseObj(), lastAlertHistoryUpdate);

        if (mutatedAlerts) {
            updateActiveAlerts();
        }

        return activeAlerts;
    }

    private void updateActiveAlerts() {
        this.activeAlerts = rollingAlerts.stream()
                .filter(activeAlert -> activeAlert.numDisplayedAreas() > 0)
                .toList();
    }

    private boolean addAlertIfNew(Alert alert, Instant now) {
        boolean mutatedAlerts = false;
        if (isNewAlert(alert)) {
            lastReceivedAlert = alert;
            List<String> newlyAlertedAreas = buildNewlyAlertedAreasList(alert, now);
            ActiveAlert activeAlert = ActiveAlertFactory.buildActiveAlert(alert, newlyAlertedAreas, now);
            rollingAlerts.addFirst(activeAlert);
            logger.info("Received alert: {} with delay {} new alerted areas: {}", alert,
                    activeAlert.alertTimestamps().getDelayReceivingAlert(),
                    activeAlert.numDisplayedAreas());
            mutatedAlerts = true;
        } else if (alert != null && rollingAlerts.isEmpty()) {
            // if the alert is not null, and it is not new, it means that the rollover time passed
            // and we still got the same alert. This seems to indicate an upstream malfunction in
            // Pekudei Oref.
            logger.info("Received repeated old alert {} id: {} at {}", alert,
                    FileTimeToInstantUtil.fileTimeToInstant(alert.id()), now);
        }
        return mutatedAlerts;
    }

    private List<String> buildNewlyAlertedAreasList(Alert alert, Instant now) {
        List<String> newlyAlertedAreas = new ArrayList<>(alert.data().size());
        for (var area : alert.data()) {
            Instant lastSeenInstant =
                    this.rollingAlertDupePreventor.putIfAbsent(new AlertedAreaWithCategory(area, alert.cat()), now);
            if (null == lastSeenInstant) {
                newlyAlertedAreas.add(area);
            }
        }
        return newlyAlertedAreas;
    }

    private boolean removeOldAlerts(Instant lastAlertHistoryUpdate) {
        Instant expirationTime = lastAlertHistoryUpdate.minus(overlapWithAlertHistory);
        boolean mutatedAlerts = false;
        // cleanup rolling alerts deque
        while (!rollingAlerts.isEmpty() &&
                rollingAlerts.peekLast().alertTimestamps().getReceivedTimestamp().isBefore(expirationTime)) {
            rollingAlerts.pollLast();
            mutatedAlerts = true;
        }

        return mutatedAlerts;
    }

    private void cleanupRepeatPreventorMap(Instant now) {
        // cleanup duplicate detection map
        Instant rolloverDupeThreshold = now.minus(repeatCityThreshold);
        var it = this.rollingAlertDupePreventor.values().iterator();
        while (it.hasNext() && it.next().isBefore(rolloverDupeThreshold)) {
            it.remove();
        }
    }

    private boolean isNewAlert(Alert a) {
        return a != null && !Objects.equals(a, lastReceivedAlert);
    }

    // package local for testing
    Collection<ActiveAlert> rollingAlerts() {
        return Collections.unmodifiableCollection(rollingAlerts);
    }

    // package local for testing
    List<ActiveAlert> activeAlerts() {
        return activeAlerts;
    }

    /**
     * A key to manage deduplicating areas displayed with rolling alerts
     *
     * @param area the area  of the alert
     * @param cat  the category of alert
     */
    private record AlertedAreaWithCategory(String area, String cat) {
    }
}
