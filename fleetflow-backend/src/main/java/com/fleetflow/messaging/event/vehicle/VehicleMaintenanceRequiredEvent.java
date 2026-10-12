package com.fleetflow.messaging.event.vehicle;

import java.time.Instant;

public record VehicleMaintenanceRequiredEvent(
        String vehicleId,
        String reasonCode,
        Instant detectedAt
) {
}