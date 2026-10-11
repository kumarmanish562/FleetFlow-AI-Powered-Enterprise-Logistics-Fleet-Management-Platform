
package com.fleetflow.tracking.dto;

import com.fleetflow.tracking.enums.TrackingEventType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateTrackingEventRequest(

        @NotNull(message = "Event type is required")
        TrackingEventType eventType,

        @NotNull(message = "Event time is required")
        OffsetDateTime eventTime,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        BigDecimal longitude,

        @Size(max = 255, message = "Location name cannot exceed 255 characters")
        String locationName,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description

) {}
