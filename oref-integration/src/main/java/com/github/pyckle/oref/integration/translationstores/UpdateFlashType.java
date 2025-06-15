package com.github.pyckle.oref.integration.translationstores;

import com.github.pyckle.oref.integration.caching.OrefApiCachingService;
import com.github.pyckle.oref.integration.dto.AlertTranslation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.GREEN_LEAVE_BUILDING;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.GREEN_NRC_LEAVE_BUILDING;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.GREEN_TERRORIST_THREAT_ENDED;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.GREEN_UAV_THREAT_ENDED;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.ORANGE_ALERTS_EXPECTED_SHORTLY;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.RED_CONTINUE_SHELTER;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.RED_NRC;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.RED_SHELTER_IMMEDIATELY;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.YELLOW_EARTHQUAKE;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.YELLOW_LEAVE_SHELTER_STAY_CLOSE;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.YELLOW_STAY_CLOSE_TO_SHELTER;
import static com.github.pyckle.oref.integration.translationstores.FlashUpdateTypeConstants.YELLOW_STAY_CLOSE_TO_SHELTER2;

public enum UpdateFlashType
{
    GREEN(100, Color.GREEN, GREEN_NRC_LEAVE_BUILDING, GREEN_LEAVE_BUILDING, GREEN_TERRORIST_THREAT_ENDED,
            GREEN_UAV_THREAT_ENDED), // safe to leave building
    YELLOW(90, Color.YELLOW, YELLOW_EARTHQUAKE, YELLOW_STAY_CLOSE_TO_SHELTER, YELLOW_STAY_CLOSE_TO_SHELTER2,
            YELLOW_LEAVE_SHELTER_STAY_CLOSE), // stay close to a shelter
    ORANGE(80, Color.ORANGE, ORANGE_ALERTS_EXPECTED_SHORTLY), // alerts expected area shortly
    RED(70, Color.RED, RED_NRC, RED_CONTINUE_SHELTER, RED_SHELTER_IMMEDIATELY); // go to shelter immediately

    private static final Set<String> HISTORICAL_LABELS = Set.of("עדכון", "מבזק");
    private static final Logger logger = LoggerFactory.getLogger(OrefApiCachingService.class);
    private final int severity;
    private final Color c;
    private final List<String> containedLabels;

    UpdateFlashType(int severity, Color c, String... containedLabels) {
        this.severity = severity;
        this.c = c;
        this.containedLabels = List.of(containedLabels);
    }

    /**
     * Find type for this update / flash
     *
     * @param hebTitle as per {@link AlertTranslation#hebTitle()}
     * @return whether this alert type matches
     */
    public static UpdateFlashType findUpdateFlashType(String hebTitle)
    {
        for (var type : values())
        {
            if (type.matches(hebTitle))
                return type;
        }

        // these are historical alerts and there's no details. default to yellow
        if (HISTORICAL_LABELS.contains(hebTitle))
            return YELLOW;

        logger.warn("Cannot find alert type for {}, defaulting to red.", hebTitle);
        return RED;
    }

    public Color getColor() {
        return c;
    }

    /**
     * Check whether this title matches this alert type
     *
     * @param hebTitle as per {@link AlertTranslation#hebTitle()}
     * @return whether this alert type matches
     */
    public boolean matches(String hebTitle)
    {
        return containedLabels.stream().anyMatch(hebTitle::contains);
    }

    public int getSeverity() {
        return severity;
    }
}
