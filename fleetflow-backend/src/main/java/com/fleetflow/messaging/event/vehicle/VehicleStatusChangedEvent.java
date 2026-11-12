package com.fleetflow.messaging.event.vehicle;

import java.time.Instant;
import java.util.UUID;

public record VehicleStatusChangedEvent(UUID vehicleId, String previousStatus, String currentStatus, Instant changedAt) {}
