package com.github.pyckle.oref.integration.activealerts;

import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.dto.Alert;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A class containing the timestamps from an alert (both local and decoded) as well as additional logic on what to display
 */
public class AlertTimestamps {
    private static final Duration REASONABLE_DELAY = Duration.ofMinutes(1);

    private final Instant receivedTimestamp;
    private final Instant decodedTimestamp;
    private final Duration delayReceivingAlert;

    /**
     * Construct a class describing timestamps for an alert
     *
     * @param receivedTimestamp the local timestamp when this alert was received
     * @param alert             the received alert (timestamp will be parsed from the ID field)
     */
    public AlertTimestamps(Instant receivedTimestamp, Alert alert) {
        this.receivedTimestamp = receivedTimestamp;
        this.decodedTimestamp = FileTimeToInstantUtil.fileTimeToInstant(alert.id());
        this.delayReceivingAlert = delayReceivingAlert(receivedTimestamp, this.decodedTimestamp);
    }


    private static Duration delayReceivingAlert(Instant receivedTimestamp, Instant decodedAlertGenerationTime) {
        Duration delayReceivingAlert = Duration.between(decodedAlertGenerationTime, receivedTimestamp);
        return delayReceivingAlert;
    }

    /**
     * Get the timestamp when this alert was received
     *
     * @return the timestamp when this alert was received
     */
    public Instant getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public String getReceivedTimestampStr() {
        return formatAsTimestamp(getReceivedTimestamp());
    }

    private static String formatAsTimestamp(Instant toFormat) {
        return OrefDateTimeUtils.toLocalTime(toFormat.truncatedTo(ChronoUnit.SECONDS)).toString();
    }

    /**
     * @return the timestamp that was decoded from the {@link Alert#id()} field
     */
    public Instant getDecodedTimestamp() {
        return decodedTimestamp;
    }

    public String getDecodedTimestampStr() {
        return formatAsTimestamp(getDecodedTimestamp());
    }

    /**
     * @return the delay between the timestamp decoded from the alert and the moment the alert was locally received.
     */
    public Duration getDelayReceivingAlert() {
        return delayReceivingAlert;
    }

    public boolean isDelayReasonable() {
        return getDelayReceivingAlert().abs().compareTo(REASONABLE_DELAY) < 0;
    }
}
