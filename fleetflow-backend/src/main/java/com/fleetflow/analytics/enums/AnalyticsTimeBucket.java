package com.fleetflow.analytics.enums;

public enum AnalyticsTimeBucket {
    DAILY("day", "YYYY-MM-DD"),
    WEEKLY("week", "IYYY-\"W\"IW"),
    MONTHLY("month", "YYYY-MM"),
    YEARLY("year", "YYYY");

    private final String postgresUnit;
    private final String labelFormat;

    AnalyticsTimeBucket(String postgresUnit, String labelFormat) {
        this.postgresUnit = postgresUnit;
        this.labelFormat = labelFormat;
    }

    public String postgresUnit() { return postgresUnit; }
    public String labelFormat() { return labelFormat; }
}
