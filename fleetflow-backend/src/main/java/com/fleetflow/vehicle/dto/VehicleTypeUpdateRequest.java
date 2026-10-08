package com.fleetflow.vehicle.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VehicleTypeUpdateRequest(

        @NotBlank(
                message = "Vehicle type name is required"
        )
        @Size(
                max = 100,
                message = "Vehicle type name must not exceed 100 characters"
        )
        String name,

        @Size(
                max = 255,
                message = "Description must not exceed 255 characters"
        )
        String description,

        @NotNull(
                message = "Maximum load is required"
        )
        @DecimalMin(
                value = "0.01",
                message = "Maximum load must be greater than zero"
        )
        BigDecimal maxLoadKg
) {
}