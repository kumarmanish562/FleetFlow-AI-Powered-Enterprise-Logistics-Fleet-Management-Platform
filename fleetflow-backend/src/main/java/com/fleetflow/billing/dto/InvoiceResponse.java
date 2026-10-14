package com.fleetflow.billing.dto;

import com.fleetflow.billing.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        Long customerId,
        Long shipmentId,
        String currency,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxableAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal outstandingBalance,
        InvoiceStatus status,
        LocalDate issueDate,
        LocalDate dueDate,
        Instant createdAt,
        List<InvoiceItemResponse> items
) {
}