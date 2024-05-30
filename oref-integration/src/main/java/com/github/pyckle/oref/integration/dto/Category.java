package com.github.pyckle.oref.integration.dto;

/**
 * Category of alerts from a service. Provides prioritization information about alert types. The higher the
 * priority the more severe the alert.
 * @param id Matches id of category in {@link HistoryEvent#category()}
 */
public record Category(int id, String category, int matrix_id, int priority, boolean queue) {
}
