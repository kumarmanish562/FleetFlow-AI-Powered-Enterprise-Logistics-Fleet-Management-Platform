package com.fleetflow.integration.routing;

import java.math.BigDecimal;

public record RouteEstimate(
        BigDecimal distanceKm,
        Integer durationMinutes,
        String provider
) {
    public RouteEstimate {
        if (distanceKm == null || distanceKm.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Distance must be positive"
            );
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Duration must be positive"
            );
        }

        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Estimate source must be provided"
            );
        }
    }
}