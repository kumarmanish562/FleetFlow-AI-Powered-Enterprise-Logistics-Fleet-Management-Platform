package com.fleetflow.vehicle.repository;

import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;


import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class VehicleRepositoryTest
        extends PostgresContainerTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    private VehicleType vehicleType;

    @BeforeEach
    void setUp() {

        vehicleType =
                vehicleTypeRepository.save(
                        VehicleType.builder()
                                .name("TRUCK")
                                .description(
                                        "Heavy transport truck"
                                )
                                .maxLoadKg(
                                        new BigDecimal("10000")
                                )
                                .build()
                );
    }

    @Test
    void shouldSaveVehicle() {

        Vehicle vehicle =
                Vehicle.builder()
                        .vehicleType(vehicleType)
                        .registrationNumber("CG04AB1234")
                        .vin("VIN123456789")
                        .manufacturer("Tata")
                        .model("Prima")
                        .manufactureYear(2025)
                        .fuelType(FuelType.DIESEL)
                        .status(VehicleStatus.AVAILABLE)
                        .currentOdometer(
                                BigDecimal.ZERO
                        )
                        .build();

        Vehicle saved =
                vehicleRepository.saveAndFlush(vehicle);

        assertThat(saved.getId())
                .isNotNull();

        assertThat(
                saved.getRegistrationNumber()
        )
                .isEqualTo("CG04AB1234");
    }

    @Test
    void shouldFindVehicleByRegistrationNumber() {

        Vehicle vehicle =
                Vehicle.builder()
                        .vehicleType(vehicleType)
                        .registrationNumber("CG04AB5678")
                        .vin("VIN987654321")
                        .manufacturer("Tata")
                        .model("Ultra")
                        .fuelType(FuelType.DIESEL)
                        .status(VehicleStatus.AVAILABLE)
                        .currentOdometer(
                                BigDecimal.ZERO
                        )
                        .build();

        vehicleRepository.saveAndFlush(vehicle);

        var result =
                vehicleRepository
                        .findByRegistrationNumber(
                                "CG04AB5678"
                        );

        assertThat(result)
                .isPresent();
    }

    @Test
    void shouldFindVehiclesByStatus() {

        Vehicle vehicle =
                Vehicle.builder()
                        .vehicleType(vehicleType)
                        .registrationNumber("CG04AB9999")
                        .vin("VIN999999999")
                        .manufacturer("Tata")
                        .model("Truck")
                        .fuelType(FuelType.DIESEL)
                        .status(VehicleStatus.AVAILABLE)
                        .currentOdometer(
                                BigDecimal.ZERO
                        )
                        .build();

        vehicleRepository.saveAndFlush(vehicle);

        List<Vehicle> vehicles =
                vehicleRepository.findByStatus(
                        VehicleStatus.AVAILABLE
                );

        assertThat(vehicles)
                .isNotEmpty();

        assertThat(vehicles)
                .anyMatch(
                        v -> v.getRegistrationNumber()
                                .equals("CG04AB9999")
                );
    }
}