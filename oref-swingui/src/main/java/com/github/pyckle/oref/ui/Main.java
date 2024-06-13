package com.github.pyckle.oref.ui;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        configSlf4j(args);
        Properties properties = parseProperties(args);
        javax.swing.SwingUtilities.invokeLater(() -> new PekudeiOrefGui(properties).createAndShowGUI());
    }

    private static Properties parseProperties(String[] args) {
        Properties properties = new Properties();
        Pattern pattern = Pattern.compile("^\\d+$");
        for (String arg : args) {
            switch (arg) {
                case "en":
                case "ru":
                case "ar":
                case "he":
                    properties.setProperty("oref.lang", arg);
                    break;
            }
            if (pattern.matcher(arg).matches()) {
                properties.setProperty("oref.font_size", arg);
            }
        }
        return properties;
    }

    private static void configSlf4j(String[] args) {
        System.getProperties().putIfAbsent("org.slf4j.simpleLogger.showDateTime", "true");
        // make the slf4j log in a more reasonable format by default
        System.getProperties().putIfAbsent("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss:SSS Z");
        if (Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("debug")))
            System.getProperties().putIfAbsent("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }
}
