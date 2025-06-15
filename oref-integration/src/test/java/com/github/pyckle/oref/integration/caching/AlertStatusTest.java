package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.alerts.details.AlertDetails;
import com.github.pyckle.oref.alerts.details.AlertSource;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.translationstores.UpdateFlashType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.pyckle.oref.integration.translationstores.UpdateFlashType.RED;
import static com.github.pyckle.oref.integration.translationstores.UpdateFlashType.YELLOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlertStatusTest
{
    @Test
    void testSimpleCase()
    {
        AlertStatus alertStatus = new AlertStatus(List.of(), Instant.now());
        ActiveAlertState state = alertStatus.activeAlertState(LocalDateTime.now());
        assertFalse(state.hasAlert());
        assertFalse(state.hasNonFlashOrUpdate());
        assertNull(state.mostSevereUpdateOrFlashType());
    }

    @Test
    void testUpdateFlash()
    {
        LocalDateTime now = LocalDateTime.now();
        AlertDetails alertDetails = dummyAlertDetails(List.of("place"), now, YELLOW);
        AlertStatus alertStatus = new AlertStatus(List.of(alertDetails), Instant.now());

        ActiveAlertState state = alertStatus.activeAlertState(now);
        assertFalse(state.hasAlert());
        assertTrue(state.hasNonFlashOrUpdate());
        assertEquals(YELLOW, state.mostSevereUpdateOrFlashType());
    }

    @Test
    void testUpdateFlashTakesMostSevere()
    {
        LocalDateTime now = LocalDateTime.now();
        AlertDetails alertDetails = dummyAlertDetails(List.of("place"), now, YELLOW);
        AlertDetails alertDetails2 = dummyAlertDetails(List.of("place"), now, RED);

        AlertStatus alertStatus = new AlertStatus(List.of(alertDetails, alertDetails2), Instant.now());
        validate(alertStatus, now);

        // opposite order
        alertStatus = new AlertStatus(List.of(alertDetails2, alertDetails), Instant.now());
        validate(alertStatus, now);
    }

    private static void validate(AlertStatus alertStatus, LocalDateTime now)
    {
        ActiveAlertState state = alertStatus.activeAlertState(now);
        assertFalse(state.hasAlert());
        assertTrue(state.hasNonFlashOrUpdate());
        assertEquals(RED, state.mostSevereUpdateOrFlashType());
    }

    AlertDetails dummyAlertDetails(List<String> locs, LocalDateTime remoteTimestamp, UpdateFlashType updateFlashType)
    {
        return new AlertDetails(AlertSource.ALERT_HISTORY,
                remoteTimestamp.atZone(OrefDateTimeUtils.ISRAEL_ZONE).toInstant(), remoteTimestamp, false,
                updateFlashType, null, null, locs, locs);
    }
}
