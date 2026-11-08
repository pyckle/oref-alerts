package com.github.pyckle.oref.integration.dto;

import java.util.List;

public record HistoryPagedApi(HistoryEvent lastRetrievedAlarm, List<AlarmsGroupedByDate> alarmsGroupedByDate) {
    public record AlarmsGroupedByDate(String key, List<HistoryEvent> value) {
    }
}
