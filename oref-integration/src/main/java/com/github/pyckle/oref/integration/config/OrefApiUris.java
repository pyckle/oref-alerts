package com.github.pyckle.oref.integration.config;

import java.net.URI;

public class OrefApiUris {
    private static final String URI_BASE = "https://www.oref.org.il";

    /**
     * Request to fetch history of historical events
     */
    public final URI historyUri;
    /**
     * Request to fetch current active alerts
     */
    public final URI alertsUri;

    /**
     * Request to fetch categories of alerts
     */
    public final URI categoriesUri;

    /**
     * Request to information about all districts
     */
    public final URI districtsUri;

    public OrefApiUris(OrefConfig orefConfig) {
        historyUri =
                URI.create(URI_BASE + "/Shared/Ajax/GetAlarmsHistory.aspx?lang=" + orefConfig.getLang() +
                        "&mode=1");
        alertsUri = URI.create(URI_BASE + "/WarningMessages/alert/alerts.json?v=1");
        categoriesUri = URI.create(URI_BASE + "/Shared/Ajax/GetAlertCategories.aspx");
        districtsUri = URI.create(
                URI_BASE + "/Shared/Ajax/GetDistricts.aspx?lang=" + orefConfig.getLang());
    }

    public URI getHistoryUri() {
        return historyUri;
    }

    public URI getAlertsUri() {
        return alertsUri;
    }

    public URI getCategoriesUri() {
        return categoriesUri;
    }

    public URI getDistrictsUri() {
        return districtsUri;
    }
}
