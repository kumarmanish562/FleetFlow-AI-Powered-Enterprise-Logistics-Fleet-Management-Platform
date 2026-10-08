package com.fleetflow.vehicle.mapper;

import com.fleetflow.vehicle.dto.VehicleTypeCreateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeResponse;
import com.fleetflow.vehicle.dto.VehicleTypeUpdateRequest;
import com.fleetflow.vehicle.entity.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTypeMapperTest {

    private VehicleTypeMapper mapper;

    @BeforeEach
    void setUp() {

        mapper =
                new VehicleTypeMapper();
    }

    @Test
    void toEntity_shouldNormalizeName() {

        VehicleTypeCreateRequest request =
                new VehicleTypeCreateRequest(
                        "  heavy   truck  ",
                        "Heavy duty truck",
                        new BigDecimal("25000")
                );

        VehicleType result =
                mapper.toEntity(request);

        assertThat(result.getName())
                .isEqualTo("HEAVY TRUCK");

        assertThat(result.getDescription())
                .isEqualTo("Heavy duty truck");

        assertThat(result.getMaxLoadKg())
                .isEqualByComparingTo("25000");
    }

    @Test
    void toEntity_shouldTrimDescription() {

        VehicleTypeCreateRequest request =
                new VehicleTypeCreateRequest(
                        "Truck",
                        "  Heavy Truck  ",
                        new BigDecimal("10000")
                );

        VehicleType result =
                mapper.toEntity(request);

        assertThat(result.getDescription())
                .isEqualTo("Heavy Truck");
    }

    @Test
    void toEntity_shouldConvertBlankDescriptionToNull() {

        VehicleTypeCreateRequest request =
                new VehicleTypeCreateRequest(
                        "Truck",
                        "   ",
                        new BigDecimal("10000")
                );

        VehicleType result =
                mapper.toEntity(request);

        assertThat(result.getDescription())
                .isNull();
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {

        VehicleType entity =
                VehicleType.builder()
                        .name("OLD")
                        .description("Old")
                        .maxLoadKg(
                                new BigDecimal("5000")
                        )
                        .build();

        VehicleTypeUpdateRequest request =
                new VehicleTypeUpdateRequest(
                        "  light van ",
                        " Updated ",
                        new BigDecimal("8000")
                );

        mapper.updateEntity(
                entity,
                request
        );

        assertThat(entity.getName())
                .isEqualTo("LIGHT VAN");

        assertThat(entity.getDescription())
                .isEqualTo("Updated");

        assertThat(entity.getMaxLoadKg())
                .isEqualByComparingTo("8000");
    }

    @Test
    void toResponse_shouldMapEntity() {

        UUID id =
                UUID.randomUUID();

        VehicleType entity =
                VehicleType.builder()
                        .name("HEAVY_TRUCK")
                        .description("Heavy truck")
                        .maxLoadKg(
                                new BigDecimal("25000")
                        )
                        .build();

        entity.setId(id);

        VehicleTypeResponse response =
                mapper.toResponse(entity);

        assertThat(response.id())
                .isEqualTo(id);

        assertThat(response.name())
                .isEqualTo("HEAVY_TRUCK");

        assertThat(response.maxLoadKg())
                .isEqualByComparingTo("25000");
    }
}