package com.fleetflow.shipment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShipmentAssignmentRequest(
        @NotNull(message = "Driver ID is required") UUID driverId,
        @NotNull(message = "Vehicle ID is required") UUID vehicleId
) {}
