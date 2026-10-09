package com.fleetflow.customer.dto;

import com.fleetflow.customer.enums.CustomerType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerCreateRequest(

        @NotNull
        CustomerType customerType,

        @Size(max = 200)
        String companyName,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @NotBlank
        @Pattern(
                regexp = "^\\+?[1-9]\\d{7,14}$",
                message = "Phone must be a valid international number"
        )
        @Size(max = 30)
        String phone,

        @Size(max = 100)
        String taxIdentifier,

        @NotNull
        @Valid
        CustomerAddressRequest billingAddress,

        @Valid
        CustomerAddressRequest shippingAddress

) {
}