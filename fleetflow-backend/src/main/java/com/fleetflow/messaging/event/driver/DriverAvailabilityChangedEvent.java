package com.fleetflow.messaging.event.driver;

import java.time.Instant;
import java.util.UUID;

public record DriverAvailabilityChangedEvent(UUID driverId, String previousAvailability,
        String currentAvailability, Instant changedAt) {}
