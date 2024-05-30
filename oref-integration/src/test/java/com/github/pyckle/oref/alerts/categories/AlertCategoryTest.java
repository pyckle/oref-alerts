package com.github.pyckle.oref.alerts.categories;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlertCategoryTest {
    @Test
    void ensureLoads() {
        assertTrue(AlertCategories.INSTANCE.getAlertCategory("1").isPresent());
    }
}
