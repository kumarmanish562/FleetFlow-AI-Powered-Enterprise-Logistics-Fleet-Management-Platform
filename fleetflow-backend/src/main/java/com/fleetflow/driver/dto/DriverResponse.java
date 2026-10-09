package com.fleetflow.driver.dto;

import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;
import com.fleetflow.driver.enums.LicenseType;

import java.time.LocalDate;
import java.util.UUID;

public record DriverResponse(

        UUID id,

        UUID userId,

        String firstName,

        String lastName,

        String phone,

        LocalDate dateOfBirth,

        String licenseNumber,

        LicenseType licenseType,

        LocalDate licenseExpiryDate,

        Integer yearsOfExperience,

        DriverEmploymentStatus employmentStatus,

        DriverAvailability availability,

        UUID assignedVehicleId,

        LocalDate hireDate,

        boolean licenseExpired,

        boolean licenseExpiringSoon
) {
}