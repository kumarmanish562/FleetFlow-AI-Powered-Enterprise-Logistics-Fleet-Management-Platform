package com.fleetflow.vehicle.mapper;

import com.fleetflow.vehicle.dto.VehicleCreateRequest;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleTypeSummary;
import com.fleetflow.vehicle.dto.VehicleUpdateRequest;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleMapperTest {

    private VehicleMapper mapper;

    private VehicleType vehicleType;

    @BeforeEach
    void setUp() {

        mapper =
                new VehicleMapper();

        vehicleType =
                VehicleType.builder()
                        .name("HEAVY_TRUCK")
                        .description("Heavy truck")
                        .maxLoadKg(
                                new BigDecimal("25000")
                        )
                        .build();

        vehicleType.setId(
                UUID.randomUUID()
        );
    }

    @Test
    void toEntity_shouldCreateAvailableVehicle() {

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        vehicleType.getId(),
                        " CG04 AB 1234 ",
                        " 1HGBH41JXMN109186 ",
                        " Tata ",
                        " Prima 3530 ",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("12500.50")
                );

        Vehicle vehicle =
                mapper.toEntity(
                        request,
                        vehicleType
                );

        assertThat(vehicle.getVehicleType())
                .isEqualTo(vehicleType);

        assertThat(
                vehicle.getRegistrationNumber()
        ).isEqualTo("CG04AB1234");

        assertThat(vehicle.getVin())
                .isEqualTo("1HGBH41JXMN109186");

        assertThat(vehicle.getManufacturer())
                .isEqualTo("Tata");

        assertThat(vehicle.getModel())
                .isEqualTo("Prima 3530");

        assertThat(vehicle.getFuelType())
                .isEqualTo(FuelType.DIESEL);

        assertThat(vehicle.getStatus())
                .isEqualTo(VehicleStatus.AVAILABLE);

        assertThat(vehicle.getCurrentOdometer())
                .isEqualByComparingTo("12500.50");
    }

    @Test
    void toEntity_nullOdometer_shouldDefaultToZero() {

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        vehicleType.getId(),
                        "CG04AB1234",
                        null,
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        null
                );

        Vehicle vehicle =
                mapper.toEntity(
                        request,
                        vehicleType
                );

        assertThat(vehicle.getCurrentOdometer())
                .isEqualByComparingTo("0");
    }

    @Test
    void updateEntity_shouldUpdateFields() {

        Vehicle vehicle =
                Vehicle.builder()
                        .vehicleType(vehicleType)
                        .registrationNumber("OLD")
                        .manufacturer("Old")
                        .model("Old Model")
                        .fuelType(FuelType.PETROL)
                        .status(VehicleStatus.AVAILABLE)
                        .currentOdometer(
                                new BigDecimal("100")
                        )
                        .build();

        VehicleUpdateRequest request =
                new VehicleUpdateRequest(
                        vehicleType.getId(),
                        "CG04XY9999",
                        "ABC123",
                        "Tata",
                        "New Model",
                        2025,
                        FuelType.DIESEL,
                        new BigDecimal("500")
                );

        mapper.updateEntity(
                vehicle,
                request,
                vehicleType
        );

        assertThat(
                vehicle.getRegistrationNumber()
        ).isEqualTo("CG04XY9999");

        assertThat(vehicle.getManufacturer())
                .isEqualTo("Tata");

        assertThat(vehicle.getModel())
                .isEqualTo("New Model");

        assertThat(vehicle.getFuelType())
                .isEqualTo(FuelType.DIESEL);

        assertThat(vehicle.getCurrentOdometer())
                .isEqualByComparingTo("500");
    }

    @Test
    void toResponse_shouldMapVehicle() {

        UUID id =
                UUID.randomUUID();

        Vehicle vehicle =
                Vehicle.builder()
                        .vehicleType(vehicleType)
                        .registrationNumber("CG04AB1234")
                        .vin("ABC123")
                        .manufacturer("Tata")
                        .model("Prima")
                        .manufactureYear(2024)
                        .fuelType(FuelType.DIESEL)
                        .status(VehicleStatus.AVAILABLE)
                        .currentOdometer(
                                new BigDecimal("1000")
                        )
                        .build();

        vehicle.setId(id);

        VehicleResponse response =
                mapper.toResponse(vehicle);

        assertThat(response.id())
                .isEqualTo(id);

        assertThat(
                response.registrationNumber()
        ).isEqualTo("CG04AB1234");

        assertThat(response.status())
                .isEqualTo(VehicleStatus.AVAILABLE);

        VehicleTypeSummary summary =
                response.vehicleType();

        assertThat(summary.id())
                .isEqualTo(vehicleType.getId());

        assertThat(summary.name())
                .isEqualTo("HEAVY_TRUCK");

        assertThat(summary.maxLoadKg())
                .isEqualByComparingTo("25000");
    }
}