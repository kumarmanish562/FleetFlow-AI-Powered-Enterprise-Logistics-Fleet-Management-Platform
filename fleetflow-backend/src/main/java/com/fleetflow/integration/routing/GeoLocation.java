package com.fleetflow.integration.routing;

import java.math.BigDecimal;

public record GeoLocation(
        BigDecimal latitude,
        BigDecimal longitude
) {
    public GeoLocation {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException(
                    "Latitude and longitude are required"
            );
        }

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("Latitude is out of range");
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("Longitude is out of range");
        }
    }
}