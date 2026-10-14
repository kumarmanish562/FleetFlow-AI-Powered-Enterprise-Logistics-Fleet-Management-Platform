package com.fleetflow.billing.dto;

import com.fleetflow.billing.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentCreateRequest(
        @NotNull UUID invoiceId,
        @NotNull PaymentMethod method,
        @NotBlank String idempotencyKey
) {
}