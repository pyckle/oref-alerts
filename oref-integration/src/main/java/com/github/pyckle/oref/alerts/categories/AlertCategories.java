package com.github.pyckle.oref.alerts.categories;

import com.github.pyckle.oref.alerts.categories.dto.AlertCategory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;

public enum AlertCategories {
    INSTANCE;
    private final Map<String, AlertCategory> categoryMap;

    AlertCategories() {
        try (var reader = new InputStreamReader(AlertCategories.class.getResourceAsStream("alertCategories.json"))) {
            this.categoryMap = Map.copyOf(new Gson().fromJson(reader, new TypeToken<>() {
            }));
        } catch (IOException ex) {
            //should not happen
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Get the AlertCategory for the given matrixId
     *
     * @param matrixId the alert category description
     * @return the alert category
     */
    public Optional<AlertCategory> getAlertCategory(String matrixId) {
        return Optional.ofNullable(categoryMap.get(matrixId));
    }

    /**
     * Check whether the alert is a drill
     *
     * @param cat the category of the alert
     * @return whether it is a drill
     */
    public boolean isDrill(int cat) {
        // this appears to be the convention used by Pekudei Oref
        return cat >= 100;
    }

    public boolean isDrill(String cat) {
        // better to assume not a drill if this is somehow set badly
        if (cat == null || cat.isEmpty() || cat.charAt(0) == '0') {
            return false;
        }

        // if anything is not a digit, assume not a drill
        for (int i = 1; i < cat.length(); i++) {
            if (cat.charAt(i) < '0' || cat.charAt(i) > '9') {
                return false;
            }
        }

        // drills are 100+, 3 digits or greater, and we already checked the first digit is not 0.
        return cat.length() >= 3;
    }
}
