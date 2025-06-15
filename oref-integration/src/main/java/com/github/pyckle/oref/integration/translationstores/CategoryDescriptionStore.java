package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.dto.AlertTranslation;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.pyckle.oref.integration.translationstores.CategoryType.CATEGORY;
import static com.github.pyckle.oref.integration.translationstores.CategoryType.MATRIX;

public class CategoryDescriptionStore
{
    private final Map<CategoryKey, AlertCategoryStore> matIdToDescription;

    public CategoryDescriptionStore(List<AlertTranslation> alertDescriptionList)
    {
        Map<CategoryKey, AlertCategoryStore.Builder> builders = new HashMap<>();
        for (AlertTranslation alertTranslation : alertDescriptionList)
        {
            builders.computeIfAbsent(new CategoryKey(CATEGORY, alertTranslation.catId()),
                            k -> new AlertCategoryStore.Builder(k.categoryType(), k.id()))
                    .addAlertTranslation(alertTranslation);
            builders.computeIfAbsent(new CategoryKey(MATRIX, alertTranslation.matrixCatId()),
                            k -> new AlertCategoryStore.Builder(k.categoryType(), k.id()))
                    .addAlertTranslation(alertTranslation);
        }
        matIdToDescription = Map.copyOf(Maps.transformValues(builders, AlertCategoryStore.Builder::build));
    }

    public String getAlertStringFromCatId(String lang, int cat, String defaultVal)
    {
        return labelOrDefault(lang, defaultVal, matIdToDescription.get(new CategoryKey(CATEGORY, cat)));
    }

    public String getAlertStringFromMatId(String lang, String cat, String defaultVal)
    {
        if (isValidInteger(cat))
            return defaultVal;
        return getAlertStringFromMatId(lang, Integer.parseInt(cat), defaultVal);
    }

    private static boolean isValidInteger(String cat)
    {
        if (cat.length() > 8)
            return true;
        for (int i = 0; i < cat.length(); i++)
        {
            if (cat.charAt(i) < '0' || cat.charAt(i) > '9')
                return true;
        }
        return false;
    }

    public String getAlertStringFromMatId(String lang, int cat, String defaultVal)
    {
        return labelOrDefault(lang, defaultVal, matIdToDescription.get(new CategoryKey(MATRIX, cat)));
    }

    private static String labelOrDefault(String lang, String defaultVal, AlertCategoryStore alertCategoryStore)
    {
        var alertTranslation = alertCategoryStore == null ? null : alertCategoryStore.getAlertTranslation(defaultVal);
        if (alertTranslation == null)
            return defaultVal;
        return alertTranslation.title(lang, defaultVal);
    }

    record CategoryKey(CategoryType categoryType, int id)
    {
    }
}
