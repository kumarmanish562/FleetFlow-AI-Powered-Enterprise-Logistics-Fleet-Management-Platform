package com.fleetflow.messaging.event.driver;

import java.time.Instant;

public record DriverAssignedEvent(
        String driverId,
        String shipmentId,
        Instant assignedAt
) {
}