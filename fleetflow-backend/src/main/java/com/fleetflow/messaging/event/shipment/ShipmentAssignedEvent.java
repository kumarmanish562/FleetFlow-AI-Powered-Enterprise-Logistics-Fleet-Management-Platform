package com.fleetflow.messaging.event.shipment;

import java.time.Instant;

public record ShipmentAssignedEvent(
        String shipmentId,
        String driverId,
        String vehicleId,
        Instant assignedAt
) {
}