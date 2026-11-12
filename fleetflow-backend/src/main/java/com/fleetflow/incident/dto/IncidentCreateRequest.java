package com.fleetflow.incident.dto;

import com.fleetflow.incident.enums.IncidentCategory;
import com.fleetflow.incident.enums.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record IncidentCreateRequest(
        @NotNull UUID shipmentId,
        @NotNull IncidentCategory category,
        @NotNull IncidentSeverity severity,
        @NotBlank @Size(max = 200) String summary,
        @Size(max = 1000) String description
) {}
