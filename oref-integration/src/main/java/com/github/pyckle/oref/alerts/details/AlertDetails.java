package com.github.pyckle.oref.alerts.details;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record AlertDetails(
        AlertSource alertSource,
        Instant receivedTimestamp,
        LocalDateTime remoteTimestamp,
        boolean isDrill,
        String category,
        String translatedCategory,
        List<String> locations,
        List<String> locationsHeb
) {
}
