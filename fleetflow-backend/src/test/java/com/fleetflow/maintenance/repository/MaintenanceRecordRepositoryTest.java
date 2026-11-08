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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MaintenanceRecordRepositoryTest extends PostgresContainerTest {

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {

        maintenanceRecordRepository.deleteAll();

        VehicleType vehicleType = VehicleType.builder()
                .name("Test Truck")
                .description("Test vehicle type")
                .maxLoadKg(new BigDecimal("10000"))
                .build();

        vehicleType = vehicleTypeRepository.saveAndFlush(vehicleType);

        vehicle = Vehicle.builder()
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

        vehicle = vehicleRepository.saveAndFlush(vehicle);
    }

    @Test
    void shouldFindMaintenanceHistory() {

        MaintenanceRecord record = new MaintenanceRecord();
        record.setVehicle(vehicle);
        record.setMaintenanceType(MaintenanceType.SERVICE);
        record.setDescription("Scheduled service test record");
        record.setMaintenanceDate(LocalDate.now());
        record.setMileage(1000L);
        record.setCost(new BigDecimal("5000"));
        record.setStatus(MaintenanceStatus.COMPLETED);

        maintenanceRecordRepository.saveAndFlush(record);

        List<MaintenanceRecord> result =
                maintenanceRecordRepository.findByVehicleIdOrderByMaintenanceDateDesc(
                        vehicle.getId()
                );

        assertThat(result).hasSize(1);

        MaintenanceRecord savedRecord = result.get(0);

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
