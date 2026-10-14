package com.fleetflow.billing.dto;

import java.math.BigDecimal;
import java.util.List;

public record BillingQuoteResponse(
        Long shipmentId,
        String currency,
        List<Charge> charges,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxableAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount
) {
    public record Charge(
            String code,
            String description,
            BigDecimal amount
    ) {
    }
}