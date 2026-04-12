package com.starrocks.admin.model.enums;

import java.time.Duration;

public enum TimeRange {
    HOURS_1("1h"),
    HOURS_6("6h"),
    HOURS_24("24h"),
    DAYS_3("3d");

    private final String value;

    TimeRange(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Duration toDuration() {
        return switch (this) {
            case HOURS_1 -> Duration.ofHours(1);
            case HOURS_6 -> Duration.ofHours(6);
            case HOURS_24 -> Duration.ofHours(24);
            case DAYS_3 -> Duration.ofDays(3);
        };
    }

    public static TimeRange fromValue(String value) {
        return switch (value) {
            case "1h" -> HOURS_1;
            case "6h" -> HOURS_6;
            case "24h" -> HOURS_24;
            case "3d" -> DAYS_3;
            default -> HOURS_24;
        };
    }
}
