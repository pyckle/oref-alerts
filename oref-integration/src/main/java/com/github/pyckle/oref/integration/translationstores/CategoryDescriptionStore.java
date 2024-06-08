package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.dto.LeftoverAlertDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDescriptionStore {
    private final Map<Integer, LeftoverAlertDescription> catIdToDescription;
    private final Map<Integer, LeftoverAlertDescription> matIdToDescription;

    public CategoryDescriptionStore(List<LeftoverAlertDescription> alertDescriptionList) {
        catIdToDescription = new HashMap<>();
        matIdToDescription = new HashMap<>();
        for (LeftoverAlertDescription lad : alertDescriptionList) {
            catIdToDescription.put(lad.category(), lad);
            matIdToDescription.put(lad.matrixid(), lad);
        }
    }

    public String getAlertStringFromCatId(int cat, String defaultVal) {
        return labelOrDefault(defaultVal, catIdToDescription.get(cat));
    }

    public String getAlertStringFromMatId(String cat, String defaultVal) {
        if (isValidInteger(cat, defaultVal))
            return defaultVal;
        return getAlertStringFromMatId(Integer.parseInt(cat), defaultVal);
    }

    private static boolean isValidInteger(String cat, String defaultVal) {
        if (cat.length() > 8)
            return true;
        for (int i = 0; i < cat.length(); i++) {
            if (cat.charAt(i) < '0' || cat.charAt(i) > '9')
                return true;
        }
        return false;
    }

    public String getAlertStringFromMatId(int cat, String defaultVal) {
        return labelOrDefault(defaultVal, matIdToDescription.get(cat));
    }

    private static String labelOrDefault(String defaultVal, LeftoverAlertDescription lad) {
        if (lad == null)
            return defaultVal;
        return lad.label();
    }
}
