package com.github.pyckle.oref.integration.dto;

/**
 * Information about a district which may receive an alert
 *
 * @param label      the district name translated into the requested language
 * @param value      Appears to be a 128 bit digest (perhaps md5?), per city (multiple districts may have the same value)
 * @param id         an id for this area. Can be used to get notes about the area (<a href="https://www.oref.org.il/Shared/Ajax/GetCityNotes.aspx?lang=en&citycode=6030">...</a>)
 * @param areaid     The id of the general area of israel this district is in
 * @param areaname   The name of the area of Israel this district is in translated into the requested language
 * @param label_he   The name of the area in Hebrew
 * @param migun_time the time to get to a protected area for a missile alert
 */
public record District(String label,
                       String value,
                       String id,
                       String areaid,
                       String areaname,
                       String label_he,
                       int migun_time) {
}
