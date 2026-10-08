
package com.fleetflow.vehicle.repository;

import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository
        extends JpaRepository<Vehicle, UUID>,
        JpaSpecificationExecutor<Vehicle> {

    Optional<Vehicle> findByRegistrationNumberIgnoreCase(
            String registrationNumber
    );

    Optional<Vehicle> findByVinIgnoreCase(
            String vin
    );

    boolean existsByRegistrationNumberIgnoreCase(
            String registrationNumber
    );

    boolean existsByRegistrationNumberIgnoreCaseAndIdNot(
            String registrationNumber,
            UUID id
    );

    boolean existsByVinIgnoreCase(
            String vin
    );

    boolean existsByVinIgnoreCaseAndIdNot(
            String vin,
            UUID id
    );

    List<Vehicle> findByStatus(
            VehicleStatus status
    );

    boolean existsByVehicleType_Id(
            UUID vehicleTypeId
    );
}

