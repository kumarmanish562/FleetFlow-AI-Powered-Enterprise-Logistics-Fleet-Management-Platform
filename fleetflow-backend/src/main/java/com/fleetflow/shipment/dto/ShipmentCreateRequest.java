package com.fleetflow.shipment.dto;

import com.fleetflow.shipment.enums.ShipmentPriority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ShipmentCreateRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotBlank(message = "Origin address is required")
        @Size(max = 5000)
        String originAddress,

        @NotBlank(message = "Destination address is required")
        @Size(max = 5000)
        String destinationAddress,

        @NotNull(message = "Weight is required")
        @DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
        BigDecimal totalWeightKg,

        @NotNull(message = "Declared value is required")
        @PositiveOrZero(message = "Declared value cannot be negative")
        BigDecimal declaredValue,

        @NotNull(message = "Priority is required")
        ShipmentPriority priority,

        @Future(message = "Expected delivery must be in the future")
        OffsetDateTime expectedDeliveryDate
) {}
