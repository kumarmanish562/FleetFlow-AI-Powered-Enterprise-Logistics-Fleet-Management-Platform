package com.fleetflow.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FailDeliveryRequest(

        @NotBlank(message = "Failure reason is required")
        @Size(max = 2000)
        String failureReason

) {
}