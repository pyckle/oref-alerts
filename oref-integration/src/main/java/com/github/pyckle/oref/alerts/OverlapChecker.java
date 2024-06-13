package com.github.pyckle.oref.alerts;

import com.github.pyckle.oref.alerts.details.AlertDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class with the logic to merge the collected alert info with the AlertHistory
 */
public class OverlapChecker {
    // check for overlap for 15 seconds from first alert returned from alert24 service.
    private static final int OVERLAP_WINDOW_SECONDS = 10;
    private static final int WINDOW_FOR_DEDUPE = 20;

    private final Set<String> overlapAreas;

    // the earliest time we can consider that an alert might be included within the AlertHistory service
    private final LocalDateTime firstPossibleCollisionTime;

    // the time after which we assume that the alert must have been returned by the alert history service
    private final LocalDateTime lastOverlapTime;

    public OverlapChecker(List<AlertDetails> alert24) {
        if (alert24.isEmpty()) {
            this.overlapAreas = Set.of();
            this.firstPossibleCollisionTime = LocalDateTime.MIN;
            this.lastOverlapTime = LocalDateTime.MIN;
        } else {
            this.overlapAreas = getOverlapAreasToIgnore(alert24);

            AlertDetails firstAlert = alert24.get(0);
            firstPossibleCollisionTime = firstAlert.remoteTimestamp().plusSeconds(OVERLAP_WINDOW_SECONDS);

            // any alert with a stamp before this time is assumed to be included by the alert 24 service.
            lastOverlapTime = firstAlert.remoteTimestamp().minusSeconds(OVERLAP_WINDOW_SECONDS);
        }
    }

    private static Set<String> getOverlapAreasToIgnore(List<AlertDetails> alert24List) {
        // don't display cities that were alerted within WINDOW_FOR_DEDUPE seconds of where alerts24 starts.
        var thresholdTime = alert24List.get(0).remoteTimestamp().minusSeconds(WINDOW_FOR_DEDUPE);
        Set<String> ret = new HashSet<>();
        for (var alert : alert24List) {
            if (alert.remoteTimestamp().isAfter(thresholdTime)) {
                ret.addAll(alert.locationsHeb());
            } else {
                break;
            }
        }
        return Collections.unmodifiableSet(ret);
    }

    public AlertDetails preventOverlap(AlertDetails alert) {
        LocalDateTime remoteTimestamp = alert.remoteTimestamp();
        if (remoteTimestamp.isAfter(this.firstPossibleCollisionTime)) {
            return alert;
        }

        if (remoteTimestamp.isAfter(lastOverlapTime) && !this.overlapAreas.containsAll(alert.locationsHeb())) {
            // todo: filter alert return cities - perhaps some are overlapped and should be removed
            return alert;
        }

        return null;
    }
}
