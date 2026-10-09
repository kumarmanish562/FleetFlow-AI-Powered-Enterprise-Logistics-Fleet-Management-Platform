package com.fleetflow.driver.dto;

import com.fleetflow.driver.enums.LicenseType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DriverUpdateRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Invalid phone number"
        )
        String phone,

        @Past
        LocalDate dateOfBirth,

        @NotBlank
        @Size(max = 100)
        String licenseNumber,

        @NotNull
        LicenseType licenseType,

        @NotNull
        @Future
        LocalDate licenseExpiryDate,

        @NotNull
        @Min(0)
        Integer yearsOfExperience,

        LocalDate hireDate
) {
}