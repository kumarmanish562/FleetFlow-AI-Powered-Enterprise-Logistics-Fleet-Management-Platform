package com.fleetflow.delivery.repository;

import com.fleetflow.delivery.entity.Delivery;
import com.fleetflow.delivery.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository
        extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByShipment_Id(UUID shipmentId);

    boolean existsByShipment_Id(UUID shipmentId);

    Page<Delivery> findByStatus(
            DeliveryStatus status,
            Pageable pageable
    );
}