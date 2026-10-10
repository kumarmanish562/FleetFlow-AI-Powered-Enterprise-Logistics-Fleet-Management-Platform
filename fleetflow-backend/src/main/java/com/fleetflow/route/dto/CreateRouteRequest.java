package com.fleetflow.route.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateRouteRequest(
        @NotNull(message = "Shipment ID is required")
        UUID shipmentId,

        @NotNull(message = "Planned departure is required")
        @Future(message = "Planned departure must be in the future")
        OffsetDateTime plannedDepartureAt
) {
}