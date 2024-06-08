package com.github.pyckle.oref.integration.dto;

/**
 * @param category {@link Category#id()} or {@link LeftoverAlertDescription#category()}
 *
 *
 *   Example:
 *     <pre>
 *          {
 *                "category": 8,
 *                "code": "earthquakealert2",
 *                "duration": 10,
 *                "label": "Earthquake alert",
 *                "description1": "Immediately go out into the open. If not possible, enter the protected room or stairwell",
 *                "description2": "",
 *                "link1": "",
 *                "link2": "https://www.oref.org.il/12765-en/pakar.aspx",
 *                "matrixid": 3
 *       },
 *     </pre>
 */
public record LeftoverAlertDescription(
        int category,
        String code,
        int duration,
        String label,
        String description1,
        String description2,
        String link1,
        String link2,
        int matrixid
) {

}
