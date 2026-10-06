package com.fleetflow.tracking.repository;

import com.fleetflow.tracking.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackingEventRepository
        extends JpaRepository<TrackingEvent, UUID> {

    List<TrackingEvent>
    findByShipmentIdOrderByEventTimeDesc(
            UUID shipmentId
    );
}