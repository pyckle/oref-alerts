package com.github.pyckle.oref.integration.dto;

/**
 * DTO for alert translation service
 *
 * @param heb         directions in Hebrew
 * @param eng         directions in English
 * @param rus         directions in Russian
 * @param arb         directions in Arabic
 * @param catId       the category id corresponding to {@link Category#id()}
 * @param matrixCatId the category id corresponding to {@link Category#matrix_id()}}
 * @param hebTitle    the title of the alert in Hebrew
 * @param engTitle    the title of the alert in English
 * @param rusTitle    the title of the alert in Russian
 * @param arbTitle    the title of the alert in Arabic
 */
public record AlertTranslation(
        String heb,
        String eng,
        String rus,
        String arb,
        int catId,
        int matrixCatId,
        String hebTitle,
        String engTitle,
        String rusTitle,
        String arbTitle
) {
    public String title(String lang, String defaultStr) {
        switch (lang) {
            case "en":
                return engTitle();
            case "ru":
                return rusTitle();
            case "ar":
                return arbTitle();
            case "he":
                return hebTitle();
            default:
                return defaultStr;
        }
    }

    public String directions(String lang, String defaultStr) {
        switch (lang) {
            case "en":
                return eng();
            case "ru":
                return rus();
            case "ar":
                return arb();
            case "he":
                return heb();
            default:
                return defaultStr;
        }
    }
}
