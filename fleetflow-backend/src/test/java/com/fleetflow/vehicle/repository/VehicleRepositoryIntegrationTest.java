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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class VehicleRepositoryIntegrationTest
        extends PostgresContainerTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    private VehicleType vehicleType;

    @BeforeEach
    void setUp() {

        vehicleRepository.deleteAll();
        vehicleTypeRepository.deleteAll();

        vehicleType =
                VehicleType.builder()
                        .name(
                                "HEAVY_TRUCK_"
                                        + System.nanoTime()
                        )
                        .description(
                                "Heavy truck"
                        )
                        .maxLoadKg(
                                new BigDecimal("25000")
                        )
                        .build();

        vehicleType =
                vehicleTypeRepository.save(
                        vehicleType
                );
    }

    private Vehicle createVehicle(
            String registration,
            String vin,
            VehicleStatus status
    ) {

        return Vehicle.builder()
                .vehicleType(vehicleType)
                .registrationNumber(registration)
                .vin(vin)
                .manufacturer("Tata")
                .model("Prima")
                .manufactureYear(2024)
                .fuelType(FuelType.DIESEL)
                .status(status)
                .currentOdometer(
                        new BigDecimal("1000")
                )
                .build();
    }

    @Test
    void save_shouldPersistVehicle() {

        Vehicle vehicle =
                createVehicle(
                        "CG04AB1001",
                        "VIN1001",
                        VehicleStatus.AVAILABLE
                );

        Vehicle saved =
                vehicleRepository.save(vehicle);

        assertThat(saved.getId())
                .isNotNull();

        assertThat(
                saved.getRegistrationNumber()
        ).isEqualTo("CG04AB1001");

        assertThat(saved.getStatus())
                .isEqualTo(
                        VehicleStatus.AVAILABLE
                );
    }

    @Test
    void findByRegistration_shouldReturnVehicle() {

        Vehicle vehicle =
                vehicleRepository.save(
                        createVehicle(
                                "CG04AB1002",
                                "VIN1002",
                                VehicleStatus.AVAILABLE
                        )
                );

        var result =
                vehicleRepository
                        .findByRegistrationNumberIgnoreCase(
                                "cg04ab1002"
                        );

        assertThat(result)
                .isPresent();

        assertThat(
                result.get().getId()
        ).isEqualTo(vehicle.getId());
    }

    @Test
    void findByVin_shouldReturnVehicle() {

        vehicleRepository.save(
                createVehicle(
                        "CG04AB1003",
                        "VIN1003",
                        VehicleStatus.AVAILABLE
                )
        );

        var result =
                vehicleRepository
                        .findByVinIgnoreCase(
                                "vin1003"
                        );

        assertThat(result)
                .isPresent();

        assertThat(
                result.get().getVin()
        ).isEqualTo("VIN1003");
    }

    @Test
    void findByStatus_shouldReturnVehicles() {

        vehicleRepository.save(
                createVehicle(
                        "CG04AB1004",
                        "VIN1004",
                        VehicleStatus.AVAILABLE
                )
        );

        vehicleRepository.save(
                createVehicle(
                        "CG04AB1005",
                        "VIN1005",
                        VehicleStatus.MAINTENANCE
                )
        );

        var result =
                vehicleRepository
                        .findByStatus(
                                VehicleStatus.AVAILABLE
                        );

        assertThat(result)
                .hasSize(1);

        assertThat(
                result.get(0).getStatus()
        ).isEqualTo(
                VehicleStatus.AVAILABLE
        );
    }

    @Test
    void pagination_shouldWork() {

        for (int i = 1; i <= 5; i++) {

            vehicleRepository.save(
                    createVehicle(
                            "CG04PAGE" + i,
                            "VINPAGE" + i,
                            VehicleStatus.AVAILABLE
                    )
            );
        }

        PageRequest pageable =
                PageRequest.of(
                        0,
                        2
                );

        Page<Vehicle> page =
                vehicleRepository.findAll(
                        pageable
                );

        assertThat(
                page.getContent()
        ).hasSize(2);

        assertThat(
                page.getTotalElements()
        ).isGreaterThanOrEqualTo(5);
    }

    @Test
    void sorting_shouldWork() {

        vehicleRepository.save(
                createVehicle(
                        "CG04SORT003",
                        "VINSORT003",
                        VehicleStatus.AVAILABLE
                )
        );

        vehicleRepository.save(
                createVehicle(
                        "CG04SORT001",
                        "VINSORT001",
                        VehicleStatus.AVAILABLE
                )
        );

        vehicleRepository.save(
                createVehicle(
                        "CG04SORT002",
                        "VINSORT002",
                        VehicleStatus.AVAILABLE
                )
        );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.ASC,
                                "registrationNumber"
                        )
                );

        Page<Vehicle> page =
                vehicleRepository.findAll(
                        pageable
                );

        assertThat(
                page.getContent()
        )
                .extracting(
                        Vehicle::getRegistrationNumber
                )
                .contains(
                        "CG04SORT001",
                        "CG04SORT002",
                        "CG04SORT003"
                );
    }

    @Test
    void existsByVehicleType_shouldWork() {

        vehicleRepository.save(
                createVehicle(
                        "CG04TYPE001",
                        "VINTYPE001",
                        VehicleStatus.AVAILABLE
                )
        );

        boolean exists =
                vehicleRepository
                        .existsByVehicleType_Id(
                                vehicleType.getId()
                        );

        assertThat(exists)
                .isTrue();
    }


    @Test
    void specification_statusFilter_shouldWork() {

        vehicleRepository.save(
                createVehicle(
                        "CG04FILTER001",
                        "VINFILTER001",
                        VehicleStatus.AVAILABLE
                )
        );

        vehicleRepository.save(
                createVehicle(
                        "CG04FILTER002",
                        "VINFILTER002",
                        VehicleStatus.MAINTENANCE
                )
        );

        var specification =
                VehicleSpecification.hasStatus(
                        VehicleStatus.MAINTENANCE
                );

        Page<Vehicle> result =
                vehicleRepository.findAll(
                        specification,
                        PageRequest.of(0, 20)
                );

        assertThat(result.getContent())
                .allMatch(
                        vehicle ->
                                vehicle.getStatus()
                                        == VehicleStatus.MAINTENANCE
                );
    }

    @Test
    void specification_fuelTypeFilter_shouldWork() {

        Vehicle vehicle =
                createVehicle(
                        "CG04FUEL001",
                        "VINFUEL001",
                        VehicleStatus.AVAILABLE
                );

        vehicle.setFuelType(
                FuelType.ELECTRIC
        );

        vehicleRepository.save(vehicle);

        var specification =
                VehicleSpecification.hasFuelType(
                        FuelType.ELECTRIC
                );

        Page<Vehicle> result =
                vehicleRepository.findAll(
                        specification,
                        PageRequest.of(0, 20)
                );

        assertThat(result.getContent())
                .allMatch(
                        v ->
                                v.getFuelType()
                                        == FuelType.ELECTRIC
                );
    }

}