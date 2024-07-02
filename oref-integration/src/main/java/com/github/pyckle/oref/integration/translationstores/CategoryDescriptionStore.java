package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.dto.AlertTranslation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDescriptionStore {
    private final Map<Integer, AlertTranslation> catIdToDescription;
    private final Map<Integer, AlertTranslation> matIdToDescription;

    public CategoryDescriptionStore(List<AlertTranslation> alertDescriptionList) {
        catIdToDescription = new HashMap<>();
        matIdToDescription = new HashMap<>();
        for (AlertTranslation alertTranslation : alertDescriptionList) {
            catIdToDescription.put(alertTranslation.catId(), alertTranslation);
            matIdToDescription.put(alertTranslation.matrixCatId(), alertTranslation);
        }
    }

    public String getAlertStringFromCatId(String lang, int cat, String defaultVal) {
        return labelOrDefault(lang, defaultVal, catIdToDescription.get(cat));
    }

    public String getAlertStringFromMatId(String lang, String cat, String defaultVal) {
        if (isValidInteger(cat))
            return defaultVal;
        return getAlertStringFromMatId(lang, Integer.parseInt(cat), defaultVal);
    }

    private static boolean isValidInteger(String cat) {
        if (cat.length() > 8)
            return true;
        for (int i = 0; i < cat.length(); i++) {
            if (cat.charAt(i) < '0' || cat.charAt(i) > '9')
                return true;
        }
        return false;
    }

    public String getAlertStringFromMatId(String lang, int cat, String defaultVal) {
        return labelOrDefault(lang, defaultVal, matIdToDescription.get(cat));
    }

    private static String labelOrDefault(String lang, String defaultVal, AlertTranslation alertTranslation) {
        if (alertTranslation == null)
            return defaultVal;
        return alertTranslation.title(lang, defaultVal);
    }
}
