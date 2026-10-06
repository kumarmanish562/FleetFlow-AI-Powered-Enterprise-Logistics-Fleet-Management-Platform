package com.fleetflow.vehicle.repository;

import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository
        extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByRegistrationNumber(
            String registrationNumber
    );

    Optional<Vehicle> findByVin(String vin);

    boolean existsByRegistrationNumber(
            String registrationNumber
    );

    boolean existsByVin(String vin);

    List<Vehicle> findByStatus(
            VehicleStatus status
    );
}