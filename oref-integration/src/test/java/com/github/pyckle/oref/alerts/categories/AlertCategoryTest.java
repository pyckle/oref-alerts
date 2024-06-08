package com.github.pyckle.oref.alerts.categories;

import com.github.pyckle.oref.alerts.categories.dto.MultiLangField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlertCategoryTest {
    @Test
    void ensureLoads() {
        assertTrue(AlertCategories.INSTANCE.getAlertCategory("1").isPresent());
        MultiLangField terroristWarning = AlertCategories.INSTANCE.getAlertCategory("13").get().alertName();
        assertTrue(terroristWarning.inLang("en", "nonsense"). contains("Terror"));
        MultiLangField missileDrill = AlertCategories.INSTANCE.getAlertCategory("101").get().alertName();
        assertTrue(missileDrill.inLang("en", "nonsense"). contains("drill"));
    }
}
