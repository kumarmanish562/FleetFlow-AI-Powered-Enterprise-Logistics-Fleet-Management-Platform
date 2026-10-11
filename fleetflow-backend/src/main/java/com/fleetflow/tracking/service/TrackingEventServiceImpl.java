
package com.fleetflow.tracking.service;

import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.tracking.dto.CreateTrackingEventRequest;
import com.fleetflow.tracking.dto.TrackingEventResponse;
import com.fleetflow.tracking.entity.TrackingEvent;
import com.fleetflow.tracking.exception.InvalidTrackingEventException;
import com.fleetflow.tracking.exception.TrackingEventNotFoundException;
import com.fleetflow.tracking.mapper.TrackingEventMapper;
import com.fleetflow.tracking.repository.TrackingEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TrackingEventServiceImpl
        implements TrackingEventService {

    private final TrackingEventRepository trackingRepository;
    private final ShipmentRepository shipmentRepository;
    private final TrackingEventMapper mapper;
    private final TrackingShipmentAccess shipmentAccess;
    private final Clock clock;
    private final long futureToleranceSeconds;

    public TrackingEventServiceImpl(
            TrackingEventRepository trackingRepository,
            ShipmentRepository shipmentRepository,
            TrackingEventMapper mapper,
            TrackingShipmentAccess shipmentAccess,
            Clock clock,
            @Value("${fleetflow.tracking.future-tolerance-seconds:30}")
            long futureToleranceSeconds) {

        this.trackingRepository = trackingRepository;
        this.shipmentRepository = shipmentRepository;
        this.mapper = mapper;
        this.shipmentAccess = shipmentAccess;
        this.clock = clock;
        this.futureToleranceSeconds = futureToleranceSeconds;
    }

    @Override
    @Transactional
    public TrackingEventResponse create(
            UUID shipmentId,
            CreateTrackingEventRequest request) {

        shipmentAccess.assertCanCreate(shipmentId);

        validateRequest(request);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new InvalidTrackingEventException(
                                "Shipment not found: " + shipmentId
                        )
                );

        validateEventTime(request.eventTime());

        /*
         * TODO: Apply the actual Phase 8 shipment lifecycle rules.
         * For example, do not allow a client to claim DELIVERED
         * unless the existing delivery workflow authorizes it.
         */

        TrackingEvent event = mapper.toEntity(shipment, request);

        TrackingEvent saved = trackingRepository.save(event);

        return mapper.toResponse(saved);
    }

    @Override
    public TrackingEventResponse getById(
            UUID shipmentId,
            UUID eventId) {

        shipmentAccess.assertCanView(shipmentId);
        requireShipment(shipmentId);

        TrackingEvent event = trackingRepository
                .findByIdAndShipment_Id(eventId, shipmentId)
                .orElseThrow(() ->
                        new TrackingEventNotFoundException(eventId)
                );

        return mapper.toResponse(event);
    }

    @Override
    public TrackingEventResponse getLatest(UUID shipmentId) {
        shipmentAccess.assertCanView(shipmentId);
        requireShipment(shipmentId);

        TrackingEvent latest = trackingRepository
                .findFirstByShipment_IdOrderByEventTimeDescIdDesc(
                        shipmentId
                )
                .orElseThrow(() ->
                        new InvalidTrackingEventException(
                                "No tracking history exists for shipment: "
                                        + shipmentId
                        )
                );

        return mapper.toResponse(latest);
    }

    @Override
    public Page<TrackingEventResponse> getHistory(
            UUID shipmentId,
            Pageable pageable) {

        shipmentAccess.assertCanView(shipmentId);
        requireShipment(shipmentId);

        Pageable safePage = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 100),
                Sort.by(
                        Sort.Order.desc("eventTime"),
                        Sort.Order.desc("id")
                )
        );

        return trackingRepository
                .findByShipment_Id(shipmentId, safePage)
                .map(mapper::toResponse);
    }

    private void requireShipment(UUID shipmentId) {
        if (!shipmentRepository.existsById(shipmentId)) {
            throw new InvalidTrackingEventException(
                    "Shipment not found: " + shipmentId
            );
        }
    }

    private void validateRequest(
            CreateTrackingEventRequest request) {

        if (request == null) {
            throw new InvalidTrackingEventException(
                    "Tracking event request is required"
            );
        }

        boolean hasLatitude = request.latitude() != null;
        boolean hasLongitude = request.longitude() != null;

        if (hasLatitude != hasLongitude) {
            throw new InvalidTrackingEventException(
                    "Latitude and longitude must be provided together"
            );
        }

        if (request.eventType() == null) {
            throw new InvalidTrackingEventException(
                    "Event type is required"
            );
        }

        if (request.eventTime() == null) {
            throw new InvalidTrackingEventException(
                    "Event time is required"
            );
        }
    }

    private void validateEventTime(OffsetDateTime eventTime) {
        OffsetDateTime now = OffsetDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );

        if (eventTime.isAfter(
                now.plusSeconds(futureToleranceSeconds))) {

            throw new InvalidTrackingEventException(
                    "Event time is too far in the future"
            );
        }
    }
}
