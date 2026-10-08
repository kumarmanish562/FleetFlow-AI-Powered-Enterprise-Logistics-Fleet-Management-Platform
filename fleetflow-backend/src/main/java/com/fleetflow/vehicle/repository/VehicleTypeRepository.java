package com.fleetflow.vehicle.repository;

import com.fleetflow.vehicle.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleTypeRepository
        extends JpaRepository<VehicleType, UUID> {

    Optional<VehicleType> findByNameIgnoreCase(
            String name
    );

    boolean existsByNameIgnoreCase(
            String name
    );

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );
}