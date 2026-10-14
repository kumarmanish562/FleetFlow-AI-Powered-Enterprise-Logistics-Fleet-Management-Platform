package com.fleetflow.billing.dto;

import com.fleetflow.billing.enums.PaymentMethod;
import com.fleetflow.billing.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID invoiceId,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        PaymentStatus status,
        String provider,
        String providerReference,
        Instant paidAt,
        Instant createdAt
) {
}