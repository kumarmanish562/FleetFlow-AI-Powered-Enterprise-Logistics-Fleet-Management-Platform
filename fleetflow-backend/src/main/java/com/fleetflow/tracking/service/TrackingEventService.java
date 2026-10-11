
package com.fleetflow.tracking.service;

import com.fleetflow.tracking.dto.CreateTrackingEventRequest;
import com.fleetflow.tracking.dto.TrackingEventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TrackingEventService {

    TrackingEventResponse create(
            UUID shipmentId,
            CreateTrackingEventRequest request
    );

    TrackingEventResponse getById(
            UUID shipmentId,
            UUID eventId
    );

    TrackingEventResponse getLatest(UUID shipmentId);

    Page<TrackingEventResponse> getHistory(
            UUID shipmentId,
            Pageable pageable
    );
}
