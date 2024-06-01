package com.github.pyckle.oref.integration.district;

import com.github.pyckle.oref.integration.caching.ApiResponse;
import com.github.pyckle.oref.integration.dto.District;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class that queryable information about districts
 */
public class DistrictStore {
    private static final Logger logger = LoggerFactory.getLogger(DistrictStore.class);

    private final String lang;
    private final Map<String, District> hebrewNameToDistrictInfo;

    /**
     * A default constructor when no info is available. The alert will be returned in Hebrew
     */
    public DistrictStore() {
        this.lang = "he";
        this.hebrewNameToDistrictInfo = Map.of();
    }

    public DistrictStore(String lang, ApiResponse<List<District>> districtList) {
        this.lang = Objects.requireNonNull(lang);
        hebrewNameToDistrictInfo = districtList.responseObj().stream()
                .collect(Collectors.toUnmodifiableMap(District::label_he, Function.identity(), (a, b) -> {
                    //In case of duplicates (which does happen) use the first one. The JS code in
                    // the Pekudei Oref website does O(n) search everytime it needs to find this, which will
                    // also return the first district
                    logger.debug("District store got two districts with identical labels: {} {}. Using the first", a,
                            b);
                    return a;
                }));
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
}
