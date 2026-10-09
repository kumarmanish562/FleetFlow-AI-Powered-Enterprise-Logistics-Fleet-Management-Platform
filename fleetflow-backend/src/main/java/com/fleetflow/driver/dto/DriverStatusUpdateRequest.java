package com.fleetflow.driver.dto;

import com.fleetflow.driver.enums.DriverEmploymentStatus;

import jakarta.validation.constraints.NotNull;

public record DriverStatusUpdateRequest(

        @NotNull
        DriverEmploymentStatus employmentStatus

) {
}