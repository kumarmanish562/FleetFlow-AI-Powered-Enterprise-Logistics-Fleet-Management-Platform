package com.fleetflow.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BillingQuoteRequest(
        @NotNull
        @Positive
        Long shipmentId
) {
}