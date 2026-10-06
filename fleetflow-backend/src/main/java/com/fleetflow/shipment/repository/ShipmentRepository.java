package com.fleetflow.shipment.repository;

import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository
        extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByShipmentNumber(
            String shipmentNumber
    );

    boolean existsByShipmentNumber(
            String shipmentNumber
    );

    Page<Shipment> findByCustomerId(
            UUID customerId,
            Pageable pageable
    );

    Page<Shipment> findByStatus(
            ShipmentStatus status,
            Pageable pageable
    );

    List<Shipment> findByDriverId(
            UUID driverId
    );
}