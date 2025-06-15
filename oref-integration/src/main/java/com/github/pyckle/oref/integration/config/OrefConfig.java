package com.github.pyckle.oref.integration.config;

import java.util.Properties;
import java.util.Set;

/**
 * Configuration of Pekudei Oref View
 */
public class OrefConfig {
    public static final String ROOT_PROP = "oref";
    private static final String DEFAULT_ENABLED = "true";
    public static final String FONT_SIZE = "font_size";
    private static final String DEFAULT_FONT_SIZE = "16";
    public static final String LANG = "lang";
    private static final String DEFAULT_LANG = "he";
    public static final String ALERT_AREA = "alert_area";
    public static final String DEFAULT_ALERT_AREA = ""; // all by default

    private final boolean isEnabled;
    private final String lang;
    private final int minFontSize;
    private final String alertArea;

    public OrefConfig(Properties properties) {
        this.isEnabled = Boolean.parseBoolean(getProperty(properties, "", DEFAULT_ENABLED));
        this.minFontSize = Integer.parseInt(getProperty(properties, FONT_SIZE, DEFAULT_FONT_SIZE));
        this.lang = getProperty(properties, LANG, DEFAULT_LANG);
        this.alertArea = getProperty(properties, ALERT_AREA, DEFAULT_ALERT_AREA);
    }

    private static String getProperty(Properties properties, String prop, String defaultVal) {
        return properties.getProperty(ROOT_PROP + (prop.isEmpty() ? "" : '.' + prop), defaultVal);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getLang() {
        return lang;
    }

    public boolean isRightToLeft() {
        boolean isRightToLeft = Set.of("he", "ar").contains(getLang());
        return isRightToLeft;
    }

    public int getMinFontSize() {
        return minFontSize;
    }

    public String getAlertArea() {
        return alertArea;
    }
}
