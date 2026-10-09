package com.fleetflow.customer.dto;

import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(

        UUID id,

        String customerCode,

        CustomerType customerType,

        String companyName,

        String firstName,

        String lastName,

        String email,

        String phone,

        String taxIdentifier,

        CustomerAddressRequest billingAddress,

        CustomerAddressRequest shippingAddress,

        CustomerStatus status,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}