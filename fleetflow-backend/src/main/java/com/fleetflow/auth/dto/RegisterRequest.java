package com.fleetflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "First name is required")
        @Size(
                min = 2,
                max = 100,
                message = "First name must contain 2-100 characters"
        )
        String firstName,

        @Size(
                max = 100,
                message = "Last name cannot exceed 100 characters"
        )
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        @Size(max = 320)
        String email,

        @Size(
                max = 30,
                message = "Phone cannot exceed 30 characters"
        )
        String phone,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must contain 8-100 characters"
        )
        String password
) {
}