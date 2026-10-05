package com.fleetflow.dto;

import java.time.Instant;

public record ApplicationMetadataResponse(
        Long id,
        String applicationName,
        String environment,
        Instant createdAt,
        Instant updatedAt
) {
}