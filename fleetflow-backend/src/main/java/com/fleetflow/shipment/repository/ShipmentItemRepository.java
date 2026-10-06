package com.fleetflow.shipment.repository;

import com.fleetflow.shipment.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShipmentItemRepository
        extends JpaRepository<ShipmentItem, UUID> {

    List<ShipmentItem> findByShipmentId(
            UUID shipmentId
    );
}