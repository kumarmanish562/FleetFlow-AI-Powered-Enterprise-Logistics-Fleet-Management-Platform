package com.fleetflow.delivery.repository;

import com.fleetflow.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository
        extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByShipmentId(
            UUID shipmentId
    );
}