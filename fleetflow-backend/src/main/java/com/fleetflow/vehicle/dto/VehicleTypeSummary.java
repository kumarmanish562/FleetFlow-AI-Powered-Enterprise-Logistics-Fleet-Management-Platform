package com.fleetflow.vehicle.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleTypeSummary(

        UUID id,

        String name,

        BigDecimal maxLoadKg
) {
}