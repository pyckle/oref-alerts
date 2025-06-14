package com.github.pyckle.oref.alerts.details;

import com.github.pyckle.oref.alerts.categories.AlertCategories;
import com.github.pyckle.oref.alerts.categories.dto.AlertCategory;
import com.github.pyckle.oref.integration.activealerts.ActiveAlert;
import com.github.pyckle.oref.integration.caching.OrefApiCachingService;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.translationstores.DistrictStore;
import com.github.pyckle.oref.integration.translationstores.UpdateFlashType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class AlertDetailsFactory {
    private static final Logger logger = LoggerFactory.getLogger(AlertDetailsFactory.class);

    private final OrefConfig orefConfig;
    private final OrefApiCachingService cachingService;

    public AlertDetailsFactory(OrefConfig orefConfig, OrefApiCachingService cachingService) {
        this.orefConfig = orefConfig;
        this.cachingService = cachingService;
    }

    public AlertDetails buildAlertDetails(ActiveAlert alert) {
        LocalDateTime decodedDateTime = getRemoteTimestamp(alert);

        List<String> translatedAreas = translateAreas(alert.filteredAreasToDisplay());
        boolean isDrill = AlertCategories.INSTANCE.isDrill(alert.alertCategoryId());
        String translatedCategory = translateCategory(alert.alertCategoryId(), alert.alertCategoryHeb());
        boolean isFlashOrUpdate = cachingService.getCategoriesApi().retrievedValue().isFlashOrUpdate(alert.alertCategoryId());
        var updateFlashType = isFlashOrUpdate ? UpdateFlashType.findUpdateFlashType(alert.alertCategoryHeb()) : null;

        return new AlertDetails(AlertSource.ALERT, alert.alertTimestamps().getReceivedTimestamp(),
                decodedDateTime, isDrill, updateFlashType, alert.alertCategoryHeb(), translatedCategory,
                translatedAreas, alert.filteredAreasToDisplay());
    }

    private static LocalDateTime getRemoteTimestamp(ActiveAlert alert) {
        final Instant decodedTimestamp;

        if (alert.alertTimestamps().successfullyDecodedTimestamp()) {
            decodedTimestamp = alert.alertTimestamps().getDecodedTimestamp();
        } else {
            // In the event that we couldn't decode the remote timestamp for any reason, use the received timestamp
            // We could use the last-modified or the server time http headers, but more plumbing is needed.
            logger.debug("Unable to decode timestamp for alert {}. Falling back to received timestamp", alert);
            decodedTimestamp = alert.alertTimestamps().getReceivedTimestamp();
        }
        LocalDateTime decodedDateTime = OrefDateTimeUtils.toLocalDateTime(decodedTimestamp);
        return decodedDateTime;
    }

    private String translateCategory(String categoryId, String hebCat) {
        String translatedCategory = cachingService.getAlertDescriptions().retrievedValue()
                .getAlertStringFromMatId(orefConfig.getLang(), categoryId, hebCat);

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

    public AlertDetails buildAlertDetailsFromHistory(boolean fromArchiveHistoryService, boolean isDrill, UpdateFlashType updateFlashType,
                                                     String translatedCategory, String categoryHeb, Instant receivedDateTime,
                                                     LocalDateTime decodedDateTime, List<String> locationsHeb) {
        return new AlertDetails(fromArchiveHistoryService ? AlertSource.HISTORY : AlertSource.ALERT_HISTORY,
                receivedDateTime, decodedDateTime, isDrill, updateFlashType, categoryHeb, translatedCategory,
                translateAreas(locationsHeb), locationsHeb);
    }
}
