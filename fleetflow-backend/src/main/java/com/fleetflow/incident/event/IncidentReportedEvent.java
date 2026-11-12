package com.fleetflow.incident.event;

import java.time.Instant;
import java.util.UUID;

public record IncidentReportedEvent(UUID incidentId, UUID shipmentId, Instant occurredAt) {}
