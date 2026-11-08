package com.fleetflow.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DriverPerformanceResponse(
        UUID driverId,
        String firstName,
        String lastName,
        long assignedShipments,
        long successfulDeliveries,
        long failedDeliveries,
        long onTimeDeliveries,
        BigDecimal onTimeDeliveryPercent,
        BigDecimal averageDurationHours
) {}
