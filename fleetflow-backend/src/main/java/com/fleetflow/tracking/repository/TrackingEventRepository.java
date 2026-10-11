
package com.fleetflow.tracking.repository;

import com.fleetflow.tracking.entity.TrackingEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackingEventRepository
        extends JpaRepository<TrackingEvent, UUID> {

    // Preserve your existing repository method.
    List<TrackingEvent> findByShipmentIdOrderByEventTimeDesc(
            UUID shipmentId
    );

    // Retrieve one event using its ID and shipment ID.
    Optional<TrackingEvent> findByIdAndShipment_Id(
            UUID eventId,
            UUID shipmentId
    );

    // Latest event by business event time, not insertion time.
    Optional<TrackingEvent>
    findFirstByShipment_IdOrderByEventTimeDescIdDesc(
            UUID shipmentId
    );

    // Paginated shipment history.
    Page<TrackingEvent> findByShipment_Id(
            UUID shipmentId,
            Pageable pageable
    );

    boolean existsByShipment_Id(UUID shipmentId);
}
