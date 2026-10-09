package com.fleetflow.customer.dto;

import com.fleetflow.customer.enums.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerCreateRequest(

        @NotBlank(message = "Customer name is required")
        @Size(max = 200, message = "Name cannot exceed 200 characters")
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 320, message = "Email cannot exceed 320 characters")
        String email,

        @Pattern(
                regexp = "^\\+?[0-9() .-]{7,30}$",
                message = "Phone number format is invalid"
        )
        String phone,

        @NotNull(message = "Customer type is required")
        CustomerType customerType,

        @Size(max = 10000)
        String billingAddress,

        @Size(max = 10000)
        String shippingAddress
) {
}