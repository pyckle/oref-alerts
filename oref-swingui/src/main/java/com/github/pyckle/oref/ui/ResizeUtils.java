package com.github.pyckle.oref.ui;

import java.awt.*;

/**
 * Utilities class to determine the best font size for the specified text with the width and height requirements
 */
public class ResizeUtils {
    public static Font findBestFontSize(Graphics g, Font f, String text, int width, int height) {
        return findBestFontSize(g, f, text, width, height, Integer.MAX_VALUE);
    }

    public static Font findBestFontSize(Graphics g, Font f, String text, int width, int height, int maxFontSize) {
        Font newFont = f;
        if (maxFontSize < f.getSize()) {
            newFont = cloneFont(f, maxFontSize);
        }

        while (newFont.getSize() <= maxFontSize && doesTextFit(text, width, height, g, newFont)) {
            newFont = cloneFont(newFont, newFont.getSize() + 1);
        }
        while (!doesTextFit(text, width, height, g, newFont)) {
            newFont = cloneFont(newFont, newFont.getSize() - 1);
        }
        return newFont;
    }

    private static boolean doesTextFit(String text, int width, int height, Graphics g, Font f) {
        FontMetrics fontMetrics = g.getFontMetrics(f);
        int stringWidth = fontMetrics.stringWidth(text);
        int fontHeight = fontMetrics.getHeight();
        return stringWidth <= width && fontHeight <= height;
    }

    public static Font cloneFont(Font currFont, int newSize) {
        return new Font(currFont.getName(), currFont.getStyle(), newSize);
    }
}
