package com.github.pyckle.oref.integration.config;

import java.net.URI;
import java.util.Locale;

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

        alertsUri = URI.create(URI_BASE + "/warningMessages/alert/Alerts.json");

        // this appears to be removed. Need to replace with something else
        alertsHistoryUri = URI.create(URI_BASE_HISTORY + "warningMessages/alert/History/AlertsHistory.json");

        // we use this for translation, need to move to: https://www.oref.org.il/alerts/alertsTranslation.json
        leftoversUri = URI.create(URI_BASE + "/Leftovers/" + lang.toUpperCase(Locale.ENGLISH) + ".Leftovers.json");

        // mode 1 is 1 day.
        // mode 2 is 1 week.
        // mode 3 is 1 month.
        historyUri =
                URI.create(URI_BASE_HISTORY + "/Shared/Ajax/GetAlarmsHistory.aspx?lang=he&mode=3");

        // this appears to be removed. Need to replace with something else
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
