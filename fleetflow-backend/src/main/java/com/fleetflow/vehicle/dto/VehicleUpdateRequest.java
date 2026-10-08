package com.fleetflow.vehicle.dto;

import com.fleetflow.vehicle.enums.FuelType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleUpdateRequest(

        @NotNull(
                message = "Vehicle type is required"
        )
        UUID vehicleTypeId,

        @NotBlank(
                message = "Registration number is required"
        )
        @Size(max = 50)
        String registrationNumber,

        @Size(max = 100)
        String vin,

        @NotBlank(
                message = "Manufacturer is required"
        )
        @Size(max = 100)
        String manufacturer,

        @NotBlank(
                message = "Model is required"
        )
        @Size(max = 100)
        String model,

        @Min(
                value = 1900,
                message = "Manufacture year must be at least 1900"
        )
        Integer manufactureYear,

        @NotNull(
                message = "Fuel type is required"
        )
        FuelType fuelType,

        @NotNull(
                message = "Odometer is required"
        )
        @DecimalMin(
                value = "0.00",
                message = "Odometer cannot be negative"
        )
        BigDecimal currentOdometer
) {
}