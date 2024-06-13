package com.github.pyckle.oref.alerts;

import com.github.pyckle.oref.alerts.categories.AlertCategories;
import com.github.pyckle.oref.alerts.details.AlertDetails;
import com.github.pyckle.oref.alerts.details.AlertDetailsFactory;
import com.github.pyckle.oref.integration.activealerts.ActiveAlert;
import com.github.pyckle.oref.integration.caching.AlertStatus;
import com.github.pyckle.oref.integration.caching.CachedApiResult;
import com.github.pyckle.oref.integration.caching.OrefApiCachingService;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.dto.AlertHistory;
import com.github.pyckle.oref.integration.dto.HistoryEventWithParsedDates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This manages all alerts from all APIs and ensures they're stitched together correctly.
 */
public class AlertsManager {
    private static final Logger logger = LoggerFactory.getLogger(AlertsManager.class);

    private final OrefApiCachingService orefApiCachingService;
    private final AlertDetailsFactory alertDetailsFactory;
    private volatile AlertStatus currentAlerts = emptyStatus();

    public static AlertStatus emptyStatus() {
        return new AlertStatus(List.of(), Instant.EPOCH);
    }

    public AlertsManager(OrefConfig orefConfig, OrefApiCachingService orefApiCachingService) {
        this.orefApiCachingService = orefApiCachingService;
        this.alertDetailsFactory = new AlertDetailsFactory(orefConfig, orefApiCachingService);
    }

    /**
     * Get all recent alerts
     *
     * @return the recent alerts
     */
    public AlertStatus getAlerts() {
        return currentAlerts;
    }

    public void updateAlerts() {
        // active alerts:
        CachedApiResult<List<ActiveAlert>> alert = orefApiCachingService.getAlert();
        logger.trace("Last Updated: {}", alert.getLastUpdated());
        List<AlertDetails> activeAlerts = getAlertDetails(alert);
        List<AlertDetails> alert24Hours = getAlertHistoryAsDetails();
        List<AlertDetails> historicalAlerts = getHistoricalAlerts(alert24Hours.isEmpty() ? LocalDateTime.MAX : alert24Hours.get(alert24Hours.size() - 1).remoteTimestamp());

        List<AlertDetails> ret = new ArrayList<>(activeAlerts.size() + alert24Hours.size() + historicalAlerts.size());

        OverlapChecker overlapChecker = new OverlapChecker(alert24Hours);
        for (AlertDetails activeAlert : activeAlerts) {
            AlertDetails toAdd = overlapChecker.preventOverlap(activeAlert);
            if (toAdd != null) {
                ret.add(activeAlert);
            }
        }

        ret.addAll(alert24Hours);
        ret.addAll(historicalAlerts);

        this.currentAlerts = new AlertStatus(Collections.unmodifiableList(ret), alert.getLastUpdated());
    }

    private List<AlertDetails> getAlertDetails(CachedApiResult<List<ActiveAlert>> activeAlerts) {
        var activeAlertIt = activeAlerts.retrievedValue().iterator();
        List<AlertDetails> ret = new ArrayList<>();
        while (activeAlertIt.hasNext()) {
            ActiveAlert next = activeAlertIt.next();
            ret.add(alertDetailsFactory.buildAlertDetails(next));
        }
        return ret;
    }

    // package local for testing
    List<AlertDetails> getAlertHistoryAsDetails() {
        var alertHistoryRes = orefApiCachingService.getAlertHistory();
        List<AlertHistory> alertHistory = alertHistoryRes.retrievedValue();
        if (alertHistory == null || alertHistory.isEmpty())
            return List.of();

        List<AlertDetails> ret = new ArrayList<>();

        int category = Integer.MIN_VALUE;
        String categoryHeb = null;
        LocalDateTime groupedDateTime = LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC);
        List<String> alertedAreas = new ArrayList<>();
        for (AlertHistory ah : alertHistory) {
            LocalDateTime currDateTime = OrefDateTimeUtils.parseAlertHistoryTimestamp(ah.alertDate());
            if (category == ah.category() && groupedDateTime.equals(currDateTime)) {
                alertedAreas.add(ah.data());
            } else {
                addAlertDetails(false, alertedAreas, category, categoryHeb, ret, alertHistoryRes.getLastFetched(), groupedDateTime);
                category = ah.category();
                categoryHeb = ah.title();
                groupedDateTime = currDateTime;
                alertedAreas = new ArrayList<>();
                alertedAreas.add(ah.data());
            }
        }
        addAlertDetails(false, alertedAreas, category, categoryHeb, ret, alertHistoryRes.getLastFetched(), groupedDateTime);
        return ret;
    }

    private void addAlertDetails(boolean historicalEvent, List<String> alertedAreas, int category, String categoryHeb,
                                 List<AlertDetails> alertDetails, Instant receivedTimestamp, LocalDateTime groupedDateTime) {
        if (!alertedAreas.isEmpty()) {
            String translatedCategory = orefApiCachingService.getAlertDescriptions().retrievedValue().getAlertStringFromCatId(category, categoryHeb);
            alertDetails.add(alertDetailsFactory.buildAlertDetailsFromHistory(historicalEvent, AlertCategories.INSTANCE.isDrill(category),
                    translatedCategory, categoryHeb, receivedTimestamp, groupedDateTime, alertedAreas));
        }
    }

    private List<AlertDetails> getHistoricalAlerts(LocalDateTime lastEventReceived) {
        var history = orefApiCachingService.getHistory();
        List<HistoryEventWithParsedDates> historyList = history.retrievedValue();
        if (historyList == null || historyList.isEmpty())
            return List.of();
        List<AlertDetails> ret = new ArrayList<>();

        int category = Integer.MIN_VALUE;
        String translatedCategory = null;
        LocalDateTime groupedDateTime = LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC);
        List<String> alertedAreas = new ArrayList<>();

        // these are already sorted
        for (var event : historyList) {
            LocalDateTime eventDateTime = event.date().atTime(event.time());
            if (eventDateTime.isBefore(lastEventReceived)) {
                if (category == event.category() && groupedDateTime.equals(eventDateTime)) {
                    alertedAreas.add(event.data());
                } else {
                    addAlertDetails(true, alertedAreas, category, translatedCategory, ret, history.getLastFetched(), groupedDateTime);
                    category = event.category();
                    translatedCategory = event.category_desc();
                    groupedDateTime = eventDateTime;
                    alertedAreas = new ArrayList<>();
                    alertedAreas.add(event.data());
                }
            }
        }
        addAlertDetails(true, alertedAreas, category, translatedCategory, ret, history.getLastFetched(), groupedDateTime);
        return ret;
    }
}
