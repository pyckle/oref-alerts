package com.github.pyckle.oref.alerts;

import com.github.pyckle.oref.alerts.details.AlertDetails;
import com.github.pyckle.oref.alerts.details.AlertSource;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OverlapCheckerTest {
    @Test
    void testOverlap() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(100_000);
        AlertDetails alertDetails = dummyAlertDetails(List.of("Jerusalem", "Tel Aviv"), now);
        now = now.plusSeconds(15);
        AlertDetails alertDetails2 = dummyAlertDetails(List.of("Jerusalem", "Modiin"), now);
        now = now.plusSeconds(15);
        AlertDetails alertDetails3 = dummyAlertDetails(List.of("Haifa"), now);

        var overlapChecker = new OverlapChecker(List.of(alertDetails3, alertDetails2, alertDetails));
        assertNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Haifa", "Jerusalem"), now)));
        assertNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Haifa"), now)));
        now = now.plusSeconds(2);
        assertNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Modiin"), now)));
        assertNotNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Tel Aviv"), now)));
        assertNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Tel Aviv"), now.minusSeconds(100))));
        assertNotNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Jerusalem", "Tel Aviv"), now)));
        assertNotNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("SomewhereOverTheRainbow", "Tel Aviv"), now)));
        now = now.plusSeconds(11);
        assertNotNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Haifa"), now)));
    }
    @Test
    void testOverlapOneAlert() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(100_000);
        AlertDetails alertDetails = dummyAlertDetails(List.of("Jerusalem", "Tel Aviv"), now);
        var overlapChecker = new OverlapChecker(List.of(alertDetails));
        assertNotNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Haifa"), now)));
        assertNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Jerusalem"), now)));
        assertNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Tel Aviv"), now)));
        now = now.plusSeconds(9);
        assertNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Jerusalem", "Tel Aviv"), now)));
        now = now.plusSeconds(9);
        assertNotNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Jerusalem", "Tel Aviv"), now)));
    }

    @Test
    void testEmptyAlert24() {
        var overlapChecker = new OverlapChecker(List.of());

        LocalDateTime now = LocalDateTime.now().minusSeconds(100_000);
        assertNotNull(overlapChecker.preventOverlap(dummyAlertDetails(List.of("Tel Aviv"), now)));
    }

    AlertDetails dummyAlertDetails(List<String> locs, LocalDateTime remoteTimestamp) {
        return new AlertDetails(AlertSource.ALERT_HISTORY, remoteTimestamp.atZone(OrefDateTimeUtils.ISRAEL_ZONE).toInstant(),
                remoteTimestamp,
                false, null, null, locs, locs);
    }
}
