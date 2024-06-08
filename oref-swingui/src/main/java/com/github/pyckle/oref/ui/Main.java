package com.github.pyckle.oref.ui;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        configSlf4j(args);
        javax.swing.SwingUtilities.invokeLater(() -> new PekudeiOrefGui().createAndShowGUI());
    }

    private static void configSlf4j(String[] args) {
        System.getProperties().putIfAbsent("org.slf4j.simpleLogger.showDateTime", "true");
        // make the slf4j log in a more reasonable format by default
        System.getProperties().putIfAbsent("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss:SSS Z");
        if (Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("debug")))
            System.getProperties().putIfAbsent("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }
}
