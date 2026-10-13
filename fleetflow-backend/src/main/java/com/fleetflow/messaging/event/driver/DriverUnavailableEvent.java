package com.fleetflow.messaging.event.driver;

import java.time.Instant;

public record DriverUnavailableEvent(
        String driverId,
        String reasonCode,
        Instant occurredAt
) {
}