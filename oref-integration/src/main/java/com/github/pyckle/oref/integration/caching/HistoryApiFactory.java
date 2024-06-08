package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.integration.config.OrefApiUris;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.dto.AlertHistory;
import com.github.pyckle.oref.integration.dto.HistoryEvent;
import com.github.pyckle.oref.integration.dto.HistoryEventWithParsedDates;
import com.google.gson.reflect.TypeToken;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoryApiFactory {
    static CachedApiCall<List<HistoryEventWithParsedDates>> buildCachedHistoryApi(OrefApiUris uris) {
        return new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getHistoryUri()),
                Duration.ofHours(2),
                Duration.ofMinutes(10),
                new TypeToken<List<HistoryEvent>>() {
                },
                List.of(),
                historyEvents -> historyEvents.responseObj().stream()
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

    static CachedApiCall<List<AlertHistory>> buildCachedHistory24Api(OrefApiUris uris) {
        CachedApiCall<List<AlertHistory>> ret = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getAlertsHistoryUri()),
                Duration.ofMinutes(15),
                Duration.ofMinutes(1),
                new TypeToken<List<AlertHistory>>() {
                },
                List.of(),
                historyEvents -> sortAlertHistory(historyEvents.responseObj()));

        return ret;
    }

    private static List<AlertHistory> sortAlertHistory(List<AlertHistory> alertHistory) {
        List<AlertHistory> sortedAlertHistory = new ArrayList<>(alertHistory);

        Comparator<AlertHistory> temporalHistoryComparator = Comparator.comparing(
                alert -> OrefDateTimeUtils.parseAlertHistoryTimestamp(alert.alertDate()));// first compare date

        // then do string comparison in case date parse fails (api change perhaps?)
        temporalHistoryComparator = temporalHistoryComparator.thenComparing(AlertHistory::alertDate);
        // we want more recent alerts first.
        temporalHistoryComparator = temporalHistoryComparator.reversed();

        // then compare categories (for grouping)
        temporalHistoryComparator = temporalHistoryComparator.thenComparing(AlertHistory::category);
        // then compare city name
        temporalHistoryComparator = temporalHistoryComparator.thenComparing(AlertHistory::data);

        sortedAlertHistory.sort(temporalHistoryComparator);
        return sortedAlertHistory;
    }
}
