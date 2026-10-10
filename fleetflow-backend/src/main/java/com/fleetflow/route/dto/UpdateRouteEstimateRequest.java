package com.fleetflow.route.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateRouteEstimateRequest(
        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal distanceKm,

        @NotNull
        @Positive
        Integer estimatedDurationMinutes,

        @NotBlank
        String estimateSource
) {
}