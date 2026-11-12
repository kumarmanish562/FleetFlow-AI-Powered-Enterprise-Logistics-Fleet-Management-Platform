package com.fleetflow.incident.dto;

import com.fleetflow.incident.enums.IncidentCategory;
import com.fleetflow.incident.enums.IncidentSeverity;
import com.fleetflow.incident.enums.IncidentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IncidentResponse(UUID id, UUID shipmentId, String shipmentNumber,
        IncidentCategory category, IncidentSeverity severity, IncidentStatus status,
        String summary, String description, OffsetDateTime reportedAt, OffsetDateTime resolvedAt) {}
