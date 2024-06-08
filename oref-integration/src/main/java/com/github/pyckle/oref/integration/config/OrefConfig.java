package com.github.pyckle.oref.integration.config;

import java.util.Properties;
import java.util.Set;

public class OrefConfig {
    public static final String ROOT_PROP = "oref";
    private static final String DEFAULT_ENABLED = "true";
    public static final String MIN_FONT = "min_font_size";
    private static final String DEFAULT_MIN_FONT = "16";
    public static final String LANG = "lang";
    private static final String DEFAULT_LANG = "he";

    private final boolean isEnabled;
    private final String lang;
    private final int minFontSize;

    public OrefConfig(Properties properties) {
        this.isEnabled = Boolean.parseBoolean(getProperty(properties, "", DEFAULT_ENABLED));
        this.minFontSize = Integer.parseInt(getProperty(properties, MIN_FONT, DEFAULT_MIN_FONT));
        this.lang = getProperty(properties, LANG, DEFAULT_LANG);
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
}
