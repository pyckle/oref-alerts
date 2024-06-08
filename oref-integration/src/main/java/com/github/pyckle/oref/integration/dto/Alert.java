package com.github.pyckle.oref.integration.dto;

import java.util.List;

/**
 * The DTO returned from
 * <p>
 * example:
 * <pre>
 *     {
 *   "id": "133601362940000000",
 *   "cat": "1",
 *   "title": "ירי רקטות וטילים",
 *   "data": [
 *     "אשקלון - דרום",
 *     "אזור תעשייה הדרומי אשקלון",
 *     "זיקים",
 *     "כרמיה",
 *     "מבקיעים"
 *   ],
 *   "desc": "היכנסו למרחב המוגן ושהו בו 10 דקות"
 * }
 * </pre>
 *
 * @param id    An ID for the Event. This appears to be the number of 100s of nanoseconds since 12:00AM Jan 1, 1601.
 *              See <a href="https://stackoverflow.com/questions/10849717/what-is-the-significance-of-january-1-1601">
 *              this stackoverflow thread</a> and
 *              <a href="https://devblogs.microsoft.com/oldnewthing/20090306-00/?p=18913">this microsoft blog</a>
 * @param cat   the category of the alert. This is an integer (sent as a String) that corresponds to a warning category
 *              hardcoded within Pekudei Oref's website's JS. In other APIs, this is referred to as "matrixid" in
 *              {@link LeftoverAlertDescription#matrixid()} {@link Category#matrix_id()}
 * @param title A description of the alert category in Hebrew
 * @param data  A list of all the areas affected in Hebrew by this alert
 * @param desc  Directions on what to do in Hebrew for this alert
 */
public record Alert(String id,
                    String cat,
                    String title,
                    List<String> data,
                    String desc) {
}
