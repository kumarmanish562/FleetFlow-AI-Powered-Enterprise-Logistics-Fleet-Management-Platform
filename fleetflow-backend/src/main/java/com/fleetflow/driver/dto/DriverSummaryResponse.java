package com.fleetflow.driver.dto;

import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;

import java.util.UUID;

public record DriverSummaryResponse(

        UUID id,

        String firstName,

        String lastName,

        String licenseNumber,

        DriverEmploymentStatus employmentStatus,

        DriverAvailability availability,

        UUID assignedVehicleId
) {
}