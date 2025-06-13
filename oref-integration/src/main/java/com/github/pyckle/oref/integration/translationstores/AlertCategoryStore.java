package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.dto.AlertTranslation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

public class AlertCategoryStore
{
	private final CategoryType categoryType;
	private final int id;
	private final Map<String, AlertTranslation> hebTitle2Alert;

	private AlertCategoryStore(CategoryType categoryType, int id, Map<String, AlertTranslation> hebTitle2Alert)
	{
		this.categoryType = categoryType;
		this.id = id;
		this.hebTitle2Alert = Map.copyOf(hebTitle2Alert);
	}

	public AlertTranslation getAlertTranslation(String hebTitle)
	{
		if (hebTitle2Alert.size() == 1)
		{
			return hebTitle2Alert.values().iterator().next();
		}
		return hebTitle2Alert.get(hebTitle);
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
			map.put(requireNonNullElse(alertTranslation.hebTitle(), requireNonNullElse(alertTranslation.heb(), "")),
					alertTranslation);
		}

		AlertCategoryStore build()
		{
			return new AlertCategoryStore(categoryType, id, map);
		}
	}
}
