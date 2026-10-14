package com.fleetflow.billing.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@ConfigurationProperties(prefix = "fleetflow.billing")
public record BillingProperties(

        @NotNull @DecimalMin("0.00")
        BigDecimal basePrice,

        @NotNull @DecimalMin("0.00")
        BigDecimal pricePerKm,

        @NotNull @DecimalMin("0.00")
        BigDecimal pricePerKg,

        @NotNull @DecimalMin("0.00")
        BigDecimal expressSurcharge,

        @NotNull @DecimalMin("0.00")
        BigDecimal urgentSurcharge,

        @NotNull @DecimalMin("0.00")
        @DecimalMin("0.00")
        BigDecimal fuelSurchargePercent,

        @NotNull @DecimalMin("0.00")
        BigDecimal discountPercent,

        @NotNull @DecimalMin("0.00")
        BigDecimal taxPercent,

        @NotBlank
        String currency
) {
}