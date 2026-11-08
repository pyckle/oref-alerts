package com.github.pyckle.oref.integration.dto;

import com.github.pyckle.oref.integration.config.OrefApiUris;

/**
 * Example:  <pre>{"data":"Shamir","date":"11.05.2024","time":"19:18:34","dateTime":"2024-05-11T19:19:00","category":2,"category_Desc":"Hostile aircraft intrusion","matrix_id":6,"rid":30759},</pre>
 * A history event as returned from the history event API {@link OrefApiUris#historyUri}
 *
 * @param data          this is the location of the event in the language specified in the API call
 * @param date          the date of the event in the not standard dd.mm.YYYY format (note the <b>.</b> separator)
 * @param time          the time of the event in HH:MM:SS
 * @param dateTime     alert date in ISO 8601. Seconds appear to always be set to zero.
 * @param category      the category of the alert. This seems to correspond to ids returned in {@link OrefApiUris#categoriesUri}
 * @param category_Desc the description of the category in the language requested in tha API call
 * @param matrixId      Correspond to the hard coded categories in the Oref frontend and {@link Category#matrix_id()}
 * @param rid           Seems to be an incrementing counter, but skips numbers occasionally
 */
public record HistoryEvent(String data,
                           String date,
                           String time,
                           String dateTime,
                           int category,
                           String category_Desc,
                           int matrixId,
                           int rid) {
}
