package com.github.pyckle.oref.integration.config;

import java.net.URI;

public class OrefApiUris {
    private static final String URI_BASE = "https://www.oref.org.il";
    private static final String URI_BASE_HISTORY = "https://alerts-history.oref.org.il";

    /**
     * Request to fetch history of historical events
     */
    public final URI historyUri;
    /**
     * Request to fetch current active alerts
     */
    public final URI alertsUri;

    /**
     * URL to fetch the last day of alerts. Updated more frequently (lower cache TTL) than the GetHistory api.
     * Hebrew only (needs translation)
     */
    public final URI alertsHistoryUri;

    /**
     * Request to fetch translated directions of alerts
     */
    public final URI translationsUri;

    /**
     * Request to fetch categories of alerts
     */
    public final URI categoriesUri;

    /**
     * Request to information about all districts
     */
    public final URI districtsUri;

    public OrefApiUris(OrefConfig orefConfig) {
        String lang = orefConfig.getLang();

        alertsUri = URI.create(URI_BASE + "/warningMessages/alert/Alerts.json");

        alertsHistoryUri = URI.create(URI_BASE + "/warningMessages/alert/History/AlertsHistory.json");

        translationsUri = URI.create(URI_BASE + "/alerts/alertsTranslation.json");

        // mode 1 is 1 day.
        // mode 2 is 1 week.
        // mode 3 is 1 month.
        historyUri =
                URI.create(URI_BASE_HISTORY + "/Shared/Ajax/GetAlarmsHistory.aspx?lang=he&mode=3");

        categoriesUri = URI.create(URI_BASE + "/alerts/alertCategories.json");
        districtsUri = URI.create(
                URI_BASE_HISTORY + "/Shared/Ajax/GetDistricts.aspx?lang=" + lang);
    }


    public URI getAlertsUri() {
        return alertsUri;
    }

    public URI getAlertsHistoryUri() {
        return alertsHistoryUri;
    }

    public URI getTranslationsUri() {
        return translationsUri;
    }

    public URI getHistoryUri() {
        return historyUri;
    }

    public URI getCategoriesUri() {
        return categoriesUri;
    }

    public URI getDistrictsUri() {
        return districtsUri;
    }
}
