package com.github.pyckle.oref.integration.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * A history record with parsed dates
 *
 * @param data          {@link HistoryEvent#data()}
 * @param date          {@link HistoryEvent#date()}
 * @param time          {@link HistoryEvent#time()}
 * @param alertDate     {@link HistoryEvent#alertDate()} ()}
 * @param category      {@link HistoryEvent#category()} }
 * @param category_desc {@link HistoryEvent#category_desc()} }
 * @param matrixId      {@link HistoryEvent#category()}
 * @param rid           {@link HistoryEvent#rid()}
 */
public record HistoryEventWithParsedDates(String data, LocalDate date, LocalTime time, LocalDateTime alertDate,
                                          int category, String category_desc, int matrixId, int rid) {
    public static final DateTimeFormatter eventDateFormatterDecoder = (new DateTimeFormatterBuilder())
            .appendValue(ChronoField.DAY_OF_MONTH, 2)//
            .appendLiteral('.')//
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)//
            .appendLiteral('.')//
            .appendValue(ChronoField.YEAR, 4)//
            .toFormatter();
}
