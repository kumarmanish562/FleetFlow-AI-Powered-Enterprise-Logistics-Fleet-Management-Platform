package com.fleetflow.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record ChartDataResponse(String key, List<Point> points) {
    public record Point(String label, BigDecimal value) {}
}
