package com.fleetflow.vehicle.repository;

import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleSpecificationTest {

    @Test
    void search_shouldCreateSpecification() {

        Specification<Vehicle> specification =
                VehicleSpecification.search(
                        "Tata"
                );

        assertThat(specification)
                .isNotNull();
    }

    @Test
    void vehicleType_shouldCreateSpecification() {

        Specification<Vehicle> specification =
                VehicleSpecification.hasVehicleType(
                        UUID.randomUUID()
                );

        assertThat(specification)
                .isNotNull();
    }

    @Test
    void status_shouldCreateSpecification() {

        Specification<Vehicle> specification =
                VehicleSpecification.hasStatus(
                        VehicleStatus.AVAILABLE
                );

        assertThat(specification)
                .isNotNull();
    }

    @Test
    void fuelType_shouldCreateSpecification() {

        Specification<Vehicle> specification =
                VehicleSpecification.hasFuelType(
                        FuelType.DIESEL
                );

        assertThat(specification)
                .isNotNull();
    }

    @Test
    void manufacturer_shouldCreateSpecification() {

        Specification<Vehicle> specification =
                VehicleSpecification.hasManufacturer(
                        "Tata"
                );

        assertThat(specification)
                .isNotNull();
    }

    @Test
    void model_shouldCreateSpecification() {

        Specification<Vehicle> specification =
                VehicleSpecification.hasModel(
                        "Prima"
                );

        assertThat(specification)
                .isNotNull();
    }
}