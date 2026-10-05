package com.fleetflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMetadataRequest(

        @NotBlank(message = "Application name is required")
        @Size(max = 100, message = "Application name must not exceed 100 characters")
        String applicationName,

        @NotBlank(message = "Environment is required")
        @Size(max = 30, message = "Environment must not exceed 30 characters")

        String environment
) {
}