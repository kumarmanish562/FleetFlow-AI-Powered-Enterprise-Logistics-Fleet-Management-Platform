package com.fleetflow.driver.dto;

import com.fleetflow.driver.enums.LicenseType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record DriverCreateRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Invalid phone number"
        )
        String phone,

        @Past(
                message = "Date of birth must be in the past"
        )
        LocalDate dateOfBirth,

        @NotBlank(message = "License number is required")
        @Size(max = 100)
        String licenseNumber,

        @NotNull(message = "License type is required")
        LicenseType licenseType,

        @NotNull(message = "License expiry date is required")
        @Future(
                message = "License must be a future date"
        )
        LocalDate licenseExpiryDate,

        @NotNull(message = "Experience is required")
        @Min(
                value = 0,
                message = "Experience cannot be negative"
        )
        Integer yearsOfExperience,

        @NotNull(message = "Hire date is required")
        LocalDate hireDate
) {
}