package com.fleetflow.route.dto;

import com.fleetflow.route.enums.RouteStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RouteResponse(
        UUID id,
        String routeNumber,
        String originAddress,
        String destinationAddress,
        BigDecimal distanceKm,
        Integer estimatedDurationMinutes,
        OffsetDateTime plannedDepartureAt,
        OffsetDateTime estimatedArrivalAt,
        RouteStatus status
) {
}