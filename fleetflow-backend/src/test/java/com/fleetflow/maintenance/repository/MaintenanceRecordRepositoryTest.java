package com.fleetflow.maintenance.repository;

import com.fleetflow.maintenance.entity.MaintenanceRecord;
import com.fleetflow.maintenance.enums.MaintenanceStatus;
import com.fleetflow.maintenance.enums.MaintenanceType;
import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.repository.VehicleRepository;
import com.fleetflow.vehicle.repository.VehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MaintenanceRecordRepositoryTest
        extends PostgresContainerTest {

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {

        /*
         * Clean maintenance records first.
         */
        maintenanceRecordRepository.deleteAll();

        /*
         * Create VehicleType.
         *
         * Vehicle requires VehicleType.
         */
        VehicleType vehicleType =
                VehicleType.builder()
                        .name("Test Truck")
                        .description("Test vehicle type")
                        .maxLoadKg(new BigDecimal("10000"))
                        .build();

        vehicleType =
                vehicleTypeRepository.saveAndFlush(vehicleType);

        /*
         * Create Vehicle using the VehicleType.
         */
        vehicle =
                Vehicle.builder()
                        .vehicleType(vehicleType)
                        .registrationNumber("TEST-MR-001")
                        .vin("TESTVIN123456789")
                        .manufacturer("Tata")
                        .model("Prima")
                        .manufactureYear(2025)
                        .fuelType(FuelType.DIESEL)
                        .status(VehicleStatus.AVAILABLE)
                        .currentOdometer(new BigDecimal("1000"))
                        .build();

        vehicle =
                vehicleRepository.saveAndFlush(vehicle);
    }

    @Test
    void shouldFindMaintenanceHistory() {

        /*
         * Create maintenance record.
         */
        MaintenanceRecord record =
                MaintenanceRecord.builder()
                        .vehicle(vehicle)
                        .maintenanceType(MaintenanceType.SERVICE)
                        .maintenanceDate(LocalDate.now())
                        .cost(new BigDecimal("5000"))
                        .status(MaintenanceStatus.COMPLETED)
                        .build();

        /*
         * Save maintenance record.
         */
        maintenanceRecordRepository
                .saveAndFlush(record);

        /*
         * Find maintenance history for vehicle.
         */
        List<MaintenanceRecord> result =
                maintenanceRecordRepository
                        .findByVehicleIdOrderByMaintenanceDateDesc(
                                vehicle.getId()
                        );

        /*
         * Verify result.
         */
        assertThat(result)
                .hasSize(1);

        MaintenanceRecord savedRecord =
                result.get(0);

        assertThat(savedRecord.getVehicle())
                .isEqualTo(vehicle);

        assertThat(savedRecord.getMaintenanceType())
                .isEqualTo(MaintenanceType.SERVICE);

        assertThat(savedRecord.getMaintenanceDate())
                .isEqualTo(LocalDate.now());

        assertThat(savedRecord.getStatus())
                .isEqualTo(MaintenanceStatus.COMPLETED);

        assertThat(savedRecord.getCost())
                .isEqualByComparingTo("5000");
    }
}