package com.github.pyckle.oref.alerts.categories.dto;

import java.util.Locale;
import java.util.Objects;

public record MultiLangField(
        String en,
        String ru,
        String he,
        String ar
) {
    public String inLang(String requestedLang, String defaultVal) {
        String translated = null;
        switch (Objects.requireNonNullElse(requestedLang, "").toLowerCase(Locale.ENGLISH)) {
            case "en":
                translated = en();
                break;
            case "ru":
                translated = ru();
                break;
            case "ar":
                translated = ar();
                break;
            case "he":
                translated = he();
                break;
        }
        if (translated == null)
            return defaultVal;
        return translated;
    }
}
