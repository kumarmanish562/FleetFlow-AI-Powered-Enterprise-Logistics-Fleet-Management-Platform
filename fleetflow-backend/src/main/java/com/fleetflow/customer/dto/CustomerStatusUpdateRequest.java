package com.fleetflow.customer.dto;

import com.fleetflow.customer.enums.CustomerStatus;
import jakarta.validation.constraints.NotNull;

public record CustomerStatusUpdateRequest(

        @NotNull(message = "Customer status is required")
        CustomerStatus status
) {
}