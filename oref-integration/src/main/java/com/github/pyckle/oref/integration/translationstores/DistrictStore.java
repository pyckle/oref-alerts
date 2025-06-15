package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.caching.ApiResponse;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.dto.District;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class that queryable information about districts
 */
public class DistrictStore {
    // not sure why oref has two options for country wide alerts, but this is the status as of 20250615
    private static final String ACROSS_THE_COUNTRY = "ברחבי הארץ";
    private static final String ENTIRE_COUNTRY = "כל הארץ";

    public static final Set<String> COUNTRYWIDE_ALERT = Set.of(ACROSS_THE_COUNTRY, ENTIRE_COUNTRY);

    private static final Logger logger = LoggerFactory.getLogger(DistrictStore.class);

    private final String lang;
    private final OrefConfig orefConfig;
    private final Map<String, District> hebrewNameToDistrictInfo;
    private final District alertArea;

    /**
     * A default constructor when no info is available. The alert will be returned in Hebrew
     */
    public DistrictStore(OrefConfig orefConfig) {
        this.lang = "he";
        this.orefConfig = orefConfig;
        this.hebrewNameToDistrictInfo = Map.of();
        this.alertArea = null;
    }

    public DistrictStore(OrefConfig orefConfig, ApiResponse<List<District>> districtList) {
        this.lang = Objects.requireNonNull(orefConfig.getLang());
        this.orefConfig = orefConfig;
        hebrewNameToDistrictInfo = districtList.responseObj().stream()
                .collect(Collectors.toUnmodifiableMap(District::label_he, Function.identity(), (a, b) -> {
                    //In case of duplicates (which does happen) use the first one. The JS code in
                    // the Pekudei Oref website does O(n) search everytime it needs to find this, which will
                    // also return the first district
                    logger.debug("District store got two districts with identical labels: {} {}. Using the first", a,
                            b);
                    return a;
                }));
        if (!hebrewNameToDistrictInfo.containsKey(ACROSS_THE_COUNTRY)) {
            logger.warn("Couldnt find district info for all of the country");
        }
        this.alertArea = internalGetAlertArea();
    }

    public District getDistrictFromHebrewLabel(String hebrewDistrictLabel) {
        return this.hebrewNameToDistrictInfo.get(hebrewDistrictLabel);
    }

    /**
     * Get the translated district label. Used to translate city names in Alerts
     *
     * @param hebrewDistrictLabel the district label in Hebrew
     * @return the translated label
     */
    public String getTranslationFromHebrewLabel(String hebrewDistrictLabel) {
        if ("he".equalsIgnoreCase(lang)) {
            return hebrewDistrictLabel;
        }
        var district = getDistrictFromHebrewLabel(hebrewDistrictLabel);
        if (district != null) {
            return district.label();
        }
        logger.warn("No label found for {}", hebrewDistrictLabel);
        return hebrewDistrictLabel;
    }

    public District getAlertArea() {
        return alertArea;
    }

    private District internalGetAlertArea() {
        if (orefConfig.getAlertArea() == null)
            return null; // all of country

        if (hebrewNameToDistrictInfo.isEmpty()) {
            logger.warn("Could not find alert area {}. Districts not initialized. Showing all of country",
                    orefConfig.getAlertArea());
            return null;
        }


        // first look for hebrew key
        if (hebrewNameToDistrictInfo.containsKey(orefConfig.getAlertArea()))
            return hebrewNameToDistrictInfo.get(orefConfig.getAlertArea());

        // search for English key. Only done once per district update - perf should be fine.
        for (District d : hebrewNameToDistrictInfo.values()) {
            if (orefConfig.getAlertArea().equalsIgnoreCase(d.label())) {
                return d;
            }
        }

        logger.warn("Could not find alert area {}. Showing all of country", orefConfig.getAlertArea());
        return null;
    }
}
