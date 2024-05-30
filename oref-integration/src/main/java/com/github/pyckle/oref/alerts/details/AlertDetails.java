package com.github.pyckle.oref.alerts.details;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record AlertDetails(
        AlertSource alertSource,
        Instant receivedTimestamp,
        LocalDateTime remoteTimestamp,
        List<String> locations,
        List<String> locationsHeb
) {
}
