package com.fleetflow.vehicle.dto;

import com.fleetflow.vehicle.enums.FuelType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleCreateRequest(

        @NotNull(
                message = "Vehicle type is required"
        )
        UUID vehicleTypeId,

        @NotBlank(
                message = "Registration number is required"
        )
        @Size(
                max = 50,
                message = "Registration number must not exceed 50 characters"
        )
        String registrationNumber,

        @Size(
                max = 100,
                message = "VIN must not exceed 100 characters"
        )
        String vin,

        @NotBlank(
                message = "Manufacturer is required"
        )
        @Size(
                max = 100,
                message = "Manufacturer must not exceed 100 characters"
        )
        String manufacturer,

        @NotBlank(
                message = "Model is required"
        )
        @Size(
                max = 100,
                message = "Model must not exceed 100 characters"
        )
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

        @DecimalMin(
                value = "0.00",
                message = "Odometer cannot be negative"
        )
        BigDecimal currentOdometer
) {
}