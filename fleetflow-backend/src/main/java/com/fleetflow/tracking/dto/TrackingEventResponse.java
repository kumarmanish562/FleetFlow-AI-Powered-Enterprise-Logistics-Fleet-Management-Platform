
package com.fleetflow.tracking.dto;

import com.fleetflow.tracking.entity.TrackingEvent;
import com.fleetflow.tracking.enums.TrackingEventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TrackingEventResponse(
        UUID id,
        UUID shipmentId,
        TrackingEventType eventType,
        OffsetDateTime eventTime,
        BigDecimal latitude,
        BigDecimal longitude,
        String locationName,
        String description
) {

    public static TrackingEventResponse from(TrackingEvent event) {
        return new TrackingEventResponse(
                event.getId(),
                event.getShipment().getId(),
                event.getEventType(),
                event.getEventTime(),
                event.getLatitude(),
                event.getLongitude(),
                event.getLocationName(),
                event.getDescription()
        );
    }
}
