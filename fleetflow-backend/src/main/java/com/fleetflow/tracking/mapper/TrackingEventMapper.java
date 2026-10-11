
package com.fleetflow.tracking.mapper;

import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.tracking.dto.CreateTrackingEventRequest;
import com.fleetflow.tracking.dto.TrackingEventResponse;
import com.fleetflow.tracking.entity.TrackingEvent;
import org.springframework.stereotype.Component;

@Component
public class TrackingEventMapper {

    public TrackingEvent toEntity(
            Shipment shipment,
            CreateTrackingEventRequest request) {

        return TrackingEvent.builder()
                .shipment(shipment)
                .eventType(request.eventType())
                .eventTime(request.eventTime())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .locationName(normalize(request.locationName()))
                .description(normalize(request.description()))
                .build();
    }

    public TrackingEventResponse toResponse(TrackingEvent event) {
        return TrackingEventResponse.from(event);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
