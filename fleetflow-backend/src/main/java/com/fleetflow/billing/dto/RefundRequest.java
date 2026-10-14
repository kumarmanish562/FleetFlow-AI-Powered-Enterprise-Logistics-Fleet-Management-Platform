package com.fleetflow.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RefundRequest(
        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        String reason,

        @NotNull
        String idempotencyKey
) {
}