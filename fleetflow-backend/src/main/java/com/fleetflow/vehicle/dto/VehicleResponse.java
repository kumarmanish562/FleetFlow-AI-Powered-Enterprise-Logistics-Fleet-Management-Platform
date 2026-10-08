package com.fleetflow.vehicle.dto;

import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleResponse(

        UUID id,

        VehicleTypeSummary vehicleType,

        String registrationNumber,

        String vin,

        String manufacturer,

        String model,

        Integer manufactureYear,

        FuelType fuelType,

        VehicleStatus status,

        BigDecimal currentOdometer,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}