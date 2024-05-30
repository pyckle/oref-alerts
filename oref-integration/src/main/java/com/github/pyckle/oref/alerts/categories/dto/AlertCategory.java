package com.github.pyckle.oref.alerts.categories.dto;

public record AlertCategory(
        String img,
        MultiLangField audio,
        MultiLangField alertName,
        MultiLangField instruction) {
}
