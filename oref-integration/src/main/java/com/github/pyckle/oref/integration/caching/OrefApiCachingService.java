package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.alerts.AlertsManager;
import com.github.pyckle.oref.alerts.details.AlertDetails;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * A service that stores caching
 */
public class OrefApiCachingService {

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
                }, List.of(), alertStore::addAlert);

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
