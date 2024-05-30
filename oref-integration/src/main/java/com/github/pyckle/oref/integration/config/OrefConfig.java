package com.github.pyckle.oref.integration.config;

import java.util.Properties;
import java.util.Set;

public class OrefConfig {
    private static final String ROOT_PROP = "oref";
    private static final String DEFAULT_ENABLED = "true";
    private static final String NUM_COLS = "num_cols";
    private static final String DEFAULT_NUM_COLS = "10";
    private static final String NUM_ROWS = "num_rows";
    private static final String DEFAULT_NUM_ROWS = "30";
    private static final String LANG = "lang";
    private static final String DEFAULT_LANG = "he";

    private final boolean isEnabled;
    private final String lang;
    private final int numCols;
    private final int numRows;

    public OrefConfig(Properties properties) {
        this.isEnabled = Boolean.parseBoolean(getProperty(properties, "", DEFAULT_ENABLED));
        this.numCols = Integer.parseInt(getProperty(properties, NUM_COLS, DEFAULT_NUM_COLS));
        this.numRows = Integer.parseInt(getProperty(properties, NUM_ROWS, DEFAULT_NUM_ROWS));
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

    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return numRows;
    }
}
