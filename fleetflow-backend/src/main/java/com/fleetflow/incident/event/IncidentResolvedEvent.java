package com.fleetflow.incident.event;

import java.time.Instant;
import java.util.UUID;

public record IncidentResolvedEvent(UUID incidentId, UUID shipmentId, Instant occurredAt) {}
