package com.github.pyckle.oref.alerts.details;

import com.github.pyckle.oref.alerts.categories.AlertCategories;
import com.github.pyckle.oref.alerts.categories.dto.AlertCategory;
import com.github.pyckle.oref.integration.activealerts.ActiveAlert;
import com.github.pyckle.oref.integration.caching.OrefApiCachingService;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.translationstores.DistrictStore;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class AlertDetailsFactory {
    private final OrefConfig orefConfig;
    private final OrefApiCachingService cachingService;

    public AlertDetailsFactory(OrefConfig orefConfig, OrefApiCachingService cachingService) {
        this.orefConfig = orefConfig;
        this.cachingService = cachingService;
    }

    public AlertDetails buildAlertDetails(ActiveAlert alert) {
        Instant decodedTimestamp = alert.alertTimestamps().getDecodedTimestamp();
        LocalDateTime decodedDateTime = OrefDateTimeUtils.toLocalDateTime(decodedTimestamp);

        List<String> translatedAreas = translateAreas(alert.filteredAreasToDisplay());
        boolean isDrill = AlertCategories.INSTANCE.isDrill(alert.alertCategoryId());
        String translatedCategory = translateCategory(alert.alertCategoryId(), alert.alertCategoryHeb());
        return new AlertDetails(AlertSource.ALERT, alert.alertTimestamps().getReceivedTimestamp(),
                decodedDateTime, isDrill, alert.alertCategoryHeb(), translatedCategory,
                translatedAreas, alert.filteredAreasToDisplay());
    }

    private String translateCategory(String categoryId, String hebCat) {
        String translatedCategory = cachingService.getAlertDescriptions().retrievedValue()
                .getAlertStringFromMatId(categoryId, hebCat);

        // alert descriptions service failed. Try alert categories
        if (translatedCategory.equals(hebCat)) {
            translatedCategory = AlertCategories.INSTANCE.getAlertCategory(categoryId)
                    .map(AlertCategory::alertName)
                    .map(mls -> mls.inLang(orefConfig.getLang(), hebCat))
                    .orElse(hebCat);
        }
        return translatedCategory;
    }

    private List<String> translateAreas(List<String> areasHeb) {
        DistrictStore districtStore = cachingService.getDistrictApi().retrievedValue();
        List<String> translatedAreas = areasHeb.stream()
                .map(districtStore::getTranslationFromHebrewLabel)
                .sorted()
                .toList();
        return translatedAreas;
    }

    public AlertDetails buildAlertDetailsFromHistory(boolean fromArchiveHistoryService, boolean isDrill,
                                                     String translatedCategory, String categoryHeb, Instant receivedDateTime,
                                                     LocalDateTime decodedDateTime, List<String> locationsHeb) {
        return new AlertDetails(fromArchiveHistoryService ? AlertSource.HISTORY : AlertSource.ALERT_HISTORY, receivedDateTime,
                decodedDateTime, isDrill, categoryHeb, translatedCategory, translateAreas(locationsHeb), locationsHeb);
    }
}
