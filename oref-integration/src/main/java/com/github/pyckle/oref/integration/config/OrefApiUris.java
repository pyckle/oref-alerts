package com.github.pyckle.oref.integration.config;

import java.net.URI;
import java.util.Locale;

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
     * URL to fetch the last day of alerts. Updated more frequently (lower cache TTL) than the GetHistory api.
     * Hebrew only (needs translation)
     */
    public final URI alertsHistoryUri;

    /**
     * Request to fetch translated directions of alerts
     */
    public final URI leftoversUri;

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

        alertsUri = URI.create(URI_BASE + "/WarningMessages/alert/alerts.json?v=1");
        alertsHistoryUri = URI.create(URI_BASE + "/WarningMessages/History/AlertsHistory.json?v=1");

        leftoversUri = URI.create(URI_BASE + "/Leftovers/" + lang.toUpperCase(Locale.ENGLISH) + ".Leftovers.json");

        // mode 1 is 1 day.
        // mode 2 is 1 week.
        // mode 3 is 1 month.
        historyUri =
                URI.create(URI_BASE + "/Shared/Ajax/GetAlarmsHistory.aspx?lang=he&mode=3");

        categoriesUri = URI.create(URI_BASE + "/Shared/Ajax/GetAlertCategories.aspx");
        districtsUri = URI.create(
                URI_BASE + "/Shared/Ajax/GetDistricts.aspx?lang=" + lang);
    }


    public URI getAlertsUri() {
        return alertsUri;
    }

    public URI getAlertsHistoryUri() {
        return alertsHistoryUri;
    }

    public URI getLeftoversUri() {
        return leftoversUri;
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
