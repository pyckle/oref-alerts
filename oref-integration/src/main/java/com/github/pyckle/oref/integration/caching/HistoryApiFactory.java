package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.integration.config.OrefApiUris;
import com.github.pyckle.oref.integration.dto.HistoryEvent;
import com.github.pyckle.oref.integration.dto.HistoryEventWithParsedDates;
import com.google.gson.reflect.TypeToken;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public class HistoryApiFactory {
    static CachedApiCall<List<HistoryEventWithParsedDates>> buildCachedHistoryApi(OrefApiUris uris) {
        return new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getHistoryUri()),
                Duration.ofMinutes(5),
                Duration.ofSeconds(60),
                new TypeToken<List<HistoryEvent>>() {
                }, historyEvents -> historyEvents.responseObj().stream()
                .map(event -> {
                    LocalDate date = LocalDate.parse(event.date(),
                            HistoryEventWithParsedDates.eventDateFormatterDecoder);
                    LocalTime time = LocalTime.parse(event.time());
                    LocalDateTime alertDate = LocalDateTime.parse(event.alertDate());
                    return new HistoryEventWithParsedDates(event.data(), date, time, alertDate,
                            event.category(),
                            event.category_desc(), event.matrixId(), event.rid());
                })
                .sorted(Comparator.comparing(HistoryEventWithParsedDates::alertDate)
                        .thenComparing(HistoryEventWithParsedDates::time)
                        .thenComparing(HistoryEventWithParsedDates::category_desc)
                        .reversed()
                        .thenComparing(HistoryEventWithParsedDates::data))//
                .toList());
    }
}
