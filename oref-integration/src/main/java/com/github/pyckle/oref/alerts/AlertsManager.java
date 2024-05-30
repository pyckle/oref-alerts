package com.github.pyckle.oref.alerts;

import com.github.pyckle.oref.alerts.details.AlertDetails;
import com.github.pyckle.oref.integration.caching.OrefApiCachingService;

import java.util.List;

public class AlertsManager {
    private final OrefApiCachingService orefApiCachingService;

    public AlertsManager(OrefApiCachingService orefApiCachingService) {
        this.orefApiCachingService = orefApiCachingService;
    }

    public List<AlertDetails> getAlerts() {
        return List.of();
    }
}
