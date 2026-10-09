package com.fleetflow.driver.dto;

import com.fleetflow.driver.enums.DriverAvailability;

import jakarta.validation.constraints.NotNull;

public record DriverAvailabilityUpdateRequest(

        @NotNull
        DriverAvailability availability

) {
}