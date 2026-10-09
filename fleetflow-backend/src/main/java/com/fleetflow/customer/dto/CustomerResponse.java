package com.fleetflow.customer.dto;

import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String email,
        String phone,
        CustomerType customerType,
        CustomerStatus status,
        String billingAddress,
        String shippingAddress,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}