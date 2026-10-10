package com.fleetflow.shipment.dto;

import com.fleetflow.shipment.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShipmentStatusUpdateRequest(
        @NotNull(message = "Target status is required")
        ShipmentStatus targetStatus,

        @Size(max = 1000, message = "Failure reason is too long")
        String failureReason
) {}
