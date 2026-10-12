package com.fleetflow.messaging.event.vehicle;

import java.time.Instant;

public record VehicleAssignedEvent(
        String vehicleId,
        String shipmentId,
        String driverId,
        Instant assignedAt
) {
}