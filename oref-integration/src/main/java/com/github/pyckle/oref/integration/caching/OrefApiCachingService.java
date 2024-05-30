package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.integration.activealerts.ActiveAlert;
import com.github.pyckle.oref.integration.activealerts.AlertsRolloverStorage;
import com.github.pyckle.oref.integration.config.OrefApiUris;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.district.DistrictStore;
import com.github.pyckle.oref.integration.dto.Alert;
import com.github.pyckle.oref.integration.dto.Category;
import com.github.pyckle.oref.integration.dto.District;
import com.github.pyckle.oref.integration.dto.HistoryEventWithParsedDates;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * A service that stores caching
 */
public class OrefApiCachingService {
    private final CachedApiCall<List<HistoryEventWithParsedDates>> historyApi;
    private final CachedApiCall<List<ActiveAlert>> alertApi;
    private final CachedApiCall<List<Category>> categoriesApi;
    private final CachedApiCall<DistrictStore> districtApi;
    private final CacheUpdateThread updaterThread;

    public OrefApiCachingService(OrefConfig orefConfig) {
        OrefApiUris uris = new OrefApiUris(orefConfig);

        this.historyApi = HistoryApiFactory.buildCachedHistoryApi(uris);

        var alertStore = new AlertsRolloverStorage();
        this.alertApi = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getAlertsUri(),
                        "Referer", "https://www.oref.org.il//12481-he/Pakar.aspx",
                        "X-Requested-With", "XMLHttpRequest"),
                Duration.ofSeconds(2),
                Duration.ofSeconds(1),
                new TypeToken<Alert>() {
                }, alertStore::addAlert);
        this.categoriesApi = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(uris.getCategoriesUri()),
                Duration.ofHours(12),
                Duration.ofMinutes(30),
                new TypeToken<>() {
                });
        URI districtsUri = uris.getDistrictsUri();
        this.districtApi = new CachedApiCall<>(
                OrefHttpRequestFactory.buildRequest(districtsUri),
                Duration.ofHours(12),
                Duration.ofMinutes(30),
                new TypeToken<List<District>>() {
                }, districts -> new DistrictStore(orefConfig.getLang(), districts));
        this.updaterThread = new CacheUpdateThread(List.of(historyApi, alertApi, categoriesApi, districtApi));
        updaterThread.start();
    }

    private static Alert getTestAlert() {
        Alert alert;
        try (InputStream is = Alert.class.getResourceAsStream("exampleAlert.json")) {
            alert = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<>() {
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return alert;
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

    public CachedApiResult<List<Category>> getCategoriesApi() {
        return categoriesApi.getCachedValue();
    }

    public CachedApiResult<DistrictStore> getDistrictApi() {
        return districtApi.getCachedValue();
    }
}
