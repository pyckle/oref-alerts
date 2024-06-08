package com.github.pyckle.oref.integration.activealerts;

import com.github.pyckle.oref.integration.dto.Alert;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class AlertJsonParsingTest {
    @Test
    void ensureJsonParserHandlesAlerts() throws Exception {
        Alert alert = getAlert();
        Assertions.assertEquals(5, alert.data().size());
    }

    @Test
    void test1601EpochIdParsing() throws Exception {
        Alert alert = getAlert();
        var builtAlert = ActiveAlertFactory.buildActiveAlert(alert, List.of(), Instant.now());
        ZonedDateTime utc = builtAlert.alertTimestamps().getDecodedTimestamp().atZone(ZoneId.of("UTC"));
        Assertions.assertEquals(2024, utc.getYear());
        Assertions.assertEquals(Month.MAY, utc.getMonth());
        Assertions.assertEquals(14, utc.getDayOfMonth());
    }

    private static Alert getAlert() throws IOException {
        Alert alert;
        try (InputStream is = AlertJsonParsingTest.class.getResourceAsStream("exampleAlert.json")) {
            alert = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<>() {
            });
        }
        return alert;
    }

    /**
     * Ensure that invalid ids simply don't parse the date - and don't throw an exception
     *
     * @throws Exception
     */
    @Test
    void testInvalidIds() {
        ensureReturnsEpoch("1");
        ensureReturnsEpoch("100");
        ensureReturnsEpoch("1000000");
        ensureReturnsEpoch("10000000");
        ensureReturnsEpoch("100000000");
        ensureReturnsEpoch("chickenchicken");
        ensureReturnsEpoch("1000chicken");
        ensureReturnsEpoch("100c0000000");
        ensureReturnsEpoch("9999999999999999999999999999");
        ensureReturnsEpoch("9".repeat(999));
    }

    private static void ensureReturnsEpoch(String invalidId) {
        Alert alert = new Alert(invalidId, null, null, List.of(), "");
        var rollingAlert = ActiveAlertFactory.buildActiveAlert(alert, List.of(), Instant.now());
        Assertions.assertEquals(Instant.EPOCH, rollingAlert.alertTimestamps().getDecodedTimestamp());
    }

    @Test
    void translateIdToTimestamp() {
        System.out.println(FileTimeToInstantUtil.fileTimeToInstant("133611948290000000"));
    }
}
