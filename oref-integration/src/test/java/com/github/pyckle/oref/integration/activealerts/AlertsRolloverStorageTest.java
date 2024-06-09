package com.github.pyckle.oref.integration.activealerts;

import com.github.pyckle.oref.integration.caching.ApiResponse;
import com.github.pyckle.oref.integration.dto.Alert;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlertsRolloverStorageTest {

    @Test
    void testAddNoAlert() {
        AlertsRolloverStorage store = new AlertsRolloverStorage();
        Instant now = Instant.EPOCH.plusSeconds(1);
        assertEquals(List.of(), store.addAlert(wrapAlert(null), now, now));
        assertEquals(List.of(), store.activeAlerts());
    }

    @Test
    void testAddAlert() {
        AlertsRolloverStorage store = new AlertsRolloverStorage();
        Instant now = Instant.EPOCH.plusSeconds(1);
        Alert dummyAlert = new Alert("1", null, null, List.of("moo"), "");
        assertEquals(1, store.addAlert(wrapAlert(dummyAlert), now, now).size());
        assertEquals(1, store.activeAlerts().size());

        // tests that adding the same alert again does not duplicate it or update the received time
        Instant afterNow = now.plusSeconds(1);
        assertEquals(1, store.addAlert(wrapAlert(dummyAlert), afterNow, afterNow).size());
        assertEquals(1, store.activeAlerts().size());
        assertEquals(now, store.activeAlerts().get(0).alertTimestamps().getReceivedTimestamp());
    }

    static ApiResponse<Alert> wrapAlert(Alert a) {
        return new ApiResponse<>(null, a);
    }

    @Test
    void testExpireAlert() {
        AlertsRolloverStorage store = new AlertsRolloverStorage();
        Instant now = Instant.EPOCH.plusSeconds(1);
        Alert dummyAlert = new Alert("1", null, null, List.of("chicken"), "");
        assertEquals(1, store.addAlert(wrapAlert(dummyAlert), now, now).size());
        assertEquals(1, store.activeAlerts().size());

        Alert dummyAlert2 = new Alert("2", null, null, List.of("potato"), "");
        // add a new alert that should expire the previous alert
        now = now.plusSeconds(3600);
        assertEquals(1, store.addAlert(wrapAlert(dummyAlert2), now, now).size());
        assertEquals(1, store.activeAlerts().size());

        // add a null alert that should expire the previous alert
        now = now.plusSeconds(3600);
        assertEquals(List.of(), store.addAlert(wrapAlert(null), now, now));
        assertEquals(List.of(), store.activeAlerts());
    }

    @Test
    void testTwoAlerts() {
        AlertsRolloverStorage store = new AlertsRolloverStorage();
        Instant now = Instant.EPOCH.plusSeconds(1);
        Alert dummyAlert = new Alert("1", null, null, List.of("taco"), "");
        assertEquals(1, store.addAlert(wrapAlert(dummyAlert), now, now).size());
        assertEquals(1, store.activeAlerts().size());

        // add null alert that does nothing
        now = now.plusSeconds(2);
        assertEquals(1, store.addAlert(wrapAlert(null), now, now).size());

        Alert dummyAlert2 = new Alert("2", null, null, List.of("shwarma"), "");
        now = now.plusSeconds(1);
        assertEquals(2, store.addAlert(wrapAlert(dummyAlert2), now, now).size());
        assertEquals(2, store.activeAlerts().size());

        // add a null alert that shouldn't expire anything
        now = now.plusSeconds(1);
        assertEquals(2, store.addAlert(wrapAlert(null), now, now).size());
        assertEquals(2, store.activeAlerts().size());

        // newer alerts should come first in the list
        assertEquals(dummyAlert2, store.activeAlerts().get(0).rawAlert());
        assertEquals(dummyAlert, store.activeAlerts().get(1).rawAlert());

        // add a null alert that should expire only the first alert
        now = now.plus(AlertsRolloverStorage.overlapWithAlertHistory).minusSeconds(3);
        assertEquals(1, store.addAlert(wrapAlert(null), now, now).size());
        assertEquals(1, store.activeAlerts().size());
        assertEquals(dummyAlert2, store.activeAlerts().get(0).rawAlert());
    }

    @Test
    void testDeduplicateCities() {
        AlertsRolloverStorage store = new AlertsRolloverStorage();

        // start after epoch to ensure that these datestamps will be used rather than id
        Instant now = Instant.EPOCH.plus(Duration.ofDays(30)).plusSeconds(1);

        Alert dummyAlert = createAlert("1", List.of("Chicago"));
        assertEquals(1, store.addAlert(wrapAlert(dummyAlert), now, now).size());
        assertEquals(1, store.activeAlerts().size());

        // add null alert that does nothing
        now = now.plusSeconds(AlertsRolloverStorage.repeatCityThreshold.toSeconds() - 2);
        assertEquals(1, store.addAlert(wrapAlert(null), now, now).size());

        now = now.plusSeconds(1);
        Alert dummyAlert2 = createAlert("2", List.of("New York", "Chicago"));
        assertEquals(2, store.addAlert(wrapAlert(dummyAlert2), now, now).size());
        assertEquals(2, store.activeAlerts().size());
        // should see latest alert first, but not with city in first alert.
        assertEquals(List.of("New York"), store.activeAlerts().get(0).filteredAreasToDisplay());
        assertEquals(List.of("Chicago"), store.activeAlerts().get(1).filteredAreasToDisplay());

        now = now.plusSeconds(4);
        // add a null alert that shouldn't expire anything
        assertEquals(2, store.addAlert(wrapAlert(null), now, now).size());
        assertEquals(List.of("New York"), store.activeAlerts().get(0).filteredAreasToDisplay());
        assertEquals(List.of("Chicago"), store.activeAlerts().get(1).filteredAreasToDisplay());

        // add a null alert that should expire only the first alert
        now = now.plus(AlertsRolloverStorage.overlapWithAlertHistory).minus(AlertsRolloverStorage.repeatCityThreshold);
        assertEquals(1, store.addAlert(wrapAlert(null), now, now.plusSeconds(1)).size());
        assertEquals(1, store.activeAlerts().size());
        // Even though the first alert was removed, Chicago won't be displayed as it was first seen before.
        assertEquals(List.of("New York"), store.activeAlerts().get(0).filteredAreasToDisplay());

        // the second alert is about to expire and we're passed the threshold to display the city twice
        now = now.plusSeconds(1);
        assertEquals(2, store.addAlert(wrapAlert(createAlert("123", List.of("New York"))), now, now).size());
        assertEquals(2, store.activeAlerts().size());
        // Now that we are passed the repeat threshold, New York will be displayed twice.
        assertEquals(List.of("New York"), store.activeAlerts().get(0).filteredAreasToDisplay());
        assertEquals(List.of("New York"), store.activeAlerts().get(1).filteredAreasToDisplay());

        // make sure we updated the most recently displayed time to avoid more repeats
        now = now.plusSeconds(1);
        // note that this alert is not stored at all because the only city in it is masked, so it is empty
        assertEquals(2, store.addAlert(wrapAlert(createAlert("124", List.of("New York"))), now, now).size());
        // even though the alert is masked in active alerts, it should still be stored in the rolling alerts
        assertEquals(3, store.rollingAlerts().size());
        // Now that we are passed the repeat threshold, chicago will be displayed twice.
        assertEquals(List.of("New York"), store.activeAlerts().get(0).filteredAreasToDisplay());
        assertEquals(List.of("New York"), store.activeAlerts().get(1).filteredAreasToDisplay());

        // make sure we updated the most recently displayed time to avoid more repeats
        now = now.plusSeconds(1);
        //make sure a different category does not mask the area, even if time threshold is not met
        assertEquals(3,
                store.addAlert(wrapAlert(new Alert("125", "6", "chicken", List.of("New York"), "")), now, now).size());
        assertEquals(List.of("New York"), store.activeAlerts().get(0).filteredAreasToDisplay());
        assertEquals(List.of("New York"), store.activeAlerts().get(1).filteredAreasToDisplay());
    }

    private static Alert createAlert(String id, List<String> cities) {
        return new Alert(id, "1", "chicken", cities, "");
    }
}
