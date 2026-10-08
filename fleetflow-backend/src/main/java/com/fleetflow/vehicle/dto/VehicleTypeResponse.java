package com.fleetflow.vehicle.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleTypeResponse(

        UUID id,

        String name,

        String description,

        BigDecimal maxLoadKg,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}