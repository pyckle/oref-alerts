package com.github.pyckle.oref.integration.activealerts;

import com.github.pyckle.oref.integration.dto.Alert;

import java.util.List;

/**
 * Parsed information about an active alert
 *
 * @param alertTimestamps        the timestamps info of the alert
 * @param rawAlert               the raw alert itself
 * @param filteredAreasToDisplay the areas to display for this alert (avoids repeat display of areas
 */
public record ActiveAlert(AlertTimestamps alertTimestamps,
                          Alert rawAlert,
                          List<String> filteredAreasToDisplay) {

    /**
     * @return Get the number of alerted areas to display
     */
    public int numDisplayedAreas() {
        return filteredAreasToDisplay.size();
    }

    /**
     * @return the alert category id
     */
    public String alertCategoryId() {
        return rawAlert.cat();
    }

    /**
     * @return the alert category in hebrew (as sent in the alert)
     */
    public String alertCategoryHeb() {
        return rawAlert.title();
    }

    /**
     * @return Instructions for those affected by the alert
     */
    public String instructions() {
        return rawAlert.desc();
    }
}
