package com.fleetflow.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record InvoiceCreateRequest(
        @NotNull @Positive Long shipmentId,
        @NotNull LocalDate dueDate
) {
}