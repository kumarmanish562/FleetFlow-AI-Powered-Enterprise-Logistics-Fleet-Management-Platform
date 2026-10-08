package com.fleetflow.vehicle.mapper;

import com.fleetflow.vehicle.dto.VehicleCreateRequest;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleTypeSummary;
import com.fleetflow.vehicle.dto.VehicleUpdateRequest;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public Vehicle toEntity(
            VehicleCreateRequest request,
            VehicleType vehicleType
    ) {

        return Vehicle.builder()
                .vehicleType(vehicleType)

                .registrationNumber(
                        normalizeRegistration(
                                request.registrationNumber()
                        )
                )

                .vin(
                        normalizeVin(
                                request.vin()
                        )
                )

                .manufacturer(
                        normalizeText(
                                request.manufacturer()
                        )
                )

                .model(
                        normalizeText(
                                request.model()
                        )
                )

                .manufactureYear(
                        request.manufactureYear()
                )

                .fuelType(
                        request.fuelType()
                )

                .status(
                        VehicleStatus.AVAILABLE
                )

                .currentOdometer(
                        request.currentOdometer() == null
                                ? java.math.BigDecimal.ZERO
                                : request.currentOdometer()
                )

                .build();
    }

    public void updateEntity(
            Vehicle entity,
            VehicleUpdateRequest request,
            VehicleType vehicleType
    ) {

        entity.setVehicleType(vehicleType);

        entity.setRegistrationNumber(
                normalizeRegistration(
                        request.registrationNumber()
                )
        );

        entity.setVin(
                normalizeVin(
                        request.vin()
                )
        );

        entity.setManufacturer(
                normalizeText(
                        request.manufacturer()
                )
        );

        entity.setModel(
                normalizeText(
                        request.model()
                )
        );

        entity.setManufactureYear(
                request.manufactureYear()
        );

        entity.setFuelType(
                request.fuelType()
        );

        entity.setCurrentOdometer(
                request.currentOdometer()
        );
    }

    public VehicleResponse toResponse(
            Vehicle entity
    ) {

        VehicleType type =
                entity.getVehicleType();

        VehicleTypeSummary typeSummary =
                new VehicleTypeSummary(
                        type.getId(),
                        type.getName(),
                        type.getMaxLoadKg()
                );

        return new VehicleResponse(
                entity.getId(),
                typeSummary,
                entity.getRegistrationNumber(),
                entity.getVin(),
                entity.getManufacturer(),
                entity.getModel(),
                entity.getManufactureYear(),
                entity.getFuelType(),
                entity.getStatus(),
                entity.getCurrentOdometer(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String normalizeRegistration(
            String value
    ) {

        return value
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }

    private String normalizeVin(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim().toUpperCase();

        return normalized.isBlank()
                ? null
                : normalized;
    }

    private String normalizeText(
            String value
    ) {

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }
}