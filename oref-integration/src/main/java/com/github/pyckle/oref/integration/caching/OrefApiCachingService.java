package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.alerts.AlertsManager;
import com.github.pyckle.oref.integration.activealerts.ActiveAlert;
import com.github.pyckle.oref.integration.activealerts.AlertsRolloverStorage;
import com.github.pyckle.oref.integration.config.OrefApiUris;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.translationstores.CategoryDescriptionStore;
import com.github.pyckle.oref.integration.translationstores.DistrictStore;
import com.github.pyckle.oref.integration.dto.Alert;
import com.github.pyckle.oref.integration.dto.AlertHistory;
import com.github.pyckle.oref.integration.dto.Category;
import com.github.pyckle.oref.integration.dto.District;
import com.github.pyckle.oref.integration.dto.HistoryEventWithParsedDates;
import com.github.pyckle.oref.integration.dto.LeftoverAlertDescription;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A service that stores caching
 */
public class OrefApiCachingService {
    private static final Logger logger = LoggerFactory.getLogger(OrefApiCachingService.class);

    // these are the APIs that are used to actively check current alerts
    private final CachedApiCall<List<ActiveAlert>> alertApi;
    private final CachedApiCall<List<AlertHistory>> alertHistory;
    private final CachedApiCall<CategoryDescriptionStore> alertDescriptions;

    // These APIs are for metadata about alerts, cities, and districts
    private final CachedApiCall<List<Category>> categoriesApi;
    private final CachedApiCall<DistrictStore> districtApi;

    // This is historical alert data.
    private final CachedApiCall<List<HistoryEventWithParsedDates>> historyApi;

    private final CacheUpdateThread updaterThread;
    private final AlertsManager alertsManager;

    public OrefApiCachingService(OrefConfig orefConfig) {
        OrefApiUris uris = new OrefApiUris(orefConfig);

        this.alertHistory = HistoryApiFactory.buildCachedHistory24Api(uris);
        var alertStore = new AlertsRolloverStorage(this.alertHistory.getCachedValue()::getLastUpdated);
        this.alertApi = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getAlertsUri(),
                        "Referer", "https://www.oref.org.il//12481-he/Pakar.aspx",
                        "X-Requested-With", "XMLHttpRequest"),
                Duration.ofSeconds(2),
                Duration.ofSeconds(1),
                new TypeToken<Alert>() {
                }, List.of(), alertStore::addAlert) {
            @Override
            public UpdateResult update() throws InterruptedException {
                boolean wasInitialized = this.isInitialized();
                Instant priorFetchedTimestamp = getLastFetched();
                var ret = super.update();
                if (wasInitialized && ret.success() && largeGapInUpdate(priorFetchedTimestamp)) {
                    logger.warn("Gap in Alert update - triggering call to history to ensure info is current {} {}",
                            priorFetchedTimestamp, getLastFetched());
                    return new UpdateResult(true, alertHistory);
                }
                return ret;
            }

            private boolean largeGapInUpdate(Instant priorFetchedTimestamp) {
                return priorFetchedTimestamp.plusSeconds(20).isBefore(getLastFetched());
            }

            private Instant getLastFetched() {
                return this.getCachedValue().getLastFetched();
            }
        };

        this.alertDescriptions = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getLeftoversUri()),
                Duration.ofHours(24),
                Duration.ofHours(1),
                new TypeToken<List<LeftoverAlertDescription>>() {
                },
                new CategoryDescriptionStore(List.of()),
                resp -> new CategoryDescriptionStore(resp.responseObj()));

        this.categoriesApi = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getCategoriesUri()),
                Duration.ofHours(12),
                Duration.ofMinutes(30),
                new TypeToken<>() {
                }, List.of());

        this.districtApi = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getDistrictsUri()),
                Duration.ofHours(12),
                Duration.ofMinutes(30),
                new TypeToken<List<District>>() {
                }, new DistrictStore(),
                districts -> new DistrictStore(orefConfig.getLang(), districts));

        this.historyApi = HistoryApiFactory.buildCachedHistoryApi(uris);

        this.alertsManager = new AlertsManager(orefConfig, this);
        this.updaterThread = new CacheUpdateThread(alertsManager::updateAlerts,
                List.of(alertDescriptions, alertHistory, alertApi, historyApi, categoriesApi, districtApi));
        updaterThread.start();
    }

    public void waitForInitialization() throws InterruptedException {
        this.updaterThread.waitForCacheInitialization();
    }

    public CachedApiResult<List<HistoryEventWithParsedDates>> getHistory() {
        return historyApi.getCachedValue();
    }

    public CachedApiResult<List<ActiveAlert>> getAlert() {
        return alertApi.getCachedValue();
    }

    public CachedApiResult<List<AlertHistory>> getAlertHistory() {
        return alertHistory.getCachedValue();
    }

    public CachedApiResult<List<Category>> getCategoriesApi() {
        return categoriesApi.getCachedValue();
    }

    public CachedApiResult<DistrictStore> getDistrictApi() {
        return districtApi.getCachedValue();
    }

    public CachedApiResult<CategoryDescriptionStore> getAlertDescriptions() {
        return alertDescriptions.getCachedValue();
    }

    public AlertsManager getAlertsManager() {
        return alertsManager;
    }
}
