package com.fleetflow.billing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemResponse(
        UUID id,
        String code,
        String description,
        BigDecimal amount,
        boolean deduction
) {
}