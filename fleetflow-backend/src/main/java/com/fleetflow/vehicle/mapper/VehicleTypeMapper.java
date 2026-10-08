package com.fleetflow.vehicle.mapper;

import com.fleetflow.vehicle.dto.VehicleTypeCreateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeResponse;
import com.fleetflow.vehicle.dto.VehicleTypeUpdateRequest;
import com.fleetflow.vehicle.entity.VehicleType;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeMapper {

    public VehicleType toEntity(
            VehicleTypeCreateRequest request
    ) {

        return VehicleType.builder()
                .name(
                        normalizeName(request.name())
                )
                .description(
                        normalizeNullable(request.description())
                )
                .maxLoadKg(
                        request.maxLoadKg()
                )
                .build();
    }

    public void updateEntity(
            VehicleType entity,
            VehicleTypeUpdateRequest request
    ) {

        entity.setName(
                normalizeName(request.name())
        );

        entity.setDescription(
                normalizeNullable(request.description())
        );

        entity.setMaxLoadKg(
                request.maxLoadKg()
        );
    }

    public VehicleTypeResponse toResponse(
            VehicleType entity
    ) {

        return new VehicleTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getMaxLoadKg(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String normalizeName(
            String value
    ) {

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();
    }

    private String normalizeNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }
}