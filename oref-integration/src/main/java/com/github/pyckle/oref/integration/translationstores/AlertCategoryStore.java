package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.dto.AlertTranslation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNullElse;

public class AlertCategoryStore
{
    private final CategoryType categoryType;
    private final int id;
    private final Map<String, AlertTranslation> hebTitle2Alert;
    private final int totalAlertTranslations;

    private AlertCategoryStore(CategoryType categoryType, int id, Map<String, AlertTranslation> hebTitle2Alert)
    {
        this.categoryType = categoryType;
        this.id = id;
        this.hebTitle2Alert = Map.copyOf(hebTitle2Alert);
        totalAlertTranslations = Set.copyOf(this.hebTitle2Alert.values()).size();
    }

    public AlertTranslation getAlertTranslation(String hebText)
    {
        if (totalAlertTranslations == 1)
        {
            return hebTitle2Alert.values().iterator().next();
        }
        return hebTitle2Alert.get(hebText);
    }

    public static class Builder
    {
        private final CategoryType categoryType;
        private final int id;
        private final HashMap<String, AlertTranslation> map = new HashMap<>();

        public Builder(CategoryType categoryType, int id)
        {
            this.categoryType = categoryType;
            this.id = id;
        }

        public void addAlertTranslation(AlertTranslation alertTranslation)
        {
            map.put(requireNonNullElse(alertTranslation.heb(), ""), alertTranslation);
            map.put(requireNonNullElse(alertTranslation.hebTitle(), ""), alertTranslation);
        }

        AlertCategoryStore build()
        {
            return new AlertCategoryStore(categoryType, id, map);
        }
    }
}
