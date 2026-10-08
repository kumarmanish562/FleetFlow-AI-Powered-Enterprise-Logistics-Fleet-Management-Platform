package com.fleetflow.vehicle.service;

import com.fleetflow.common.exception.ConflictException;
import com.fleetflow.common.exception.ResourceNotFoundException;
import com.fleetflow.vehicle.dto.VehicleTypeCreateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeResponse;
import com.fleetflow.vehicle.dto.VehicleTypeUpdateRequest;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.mapper.VehicleTypeMapper;
import com.fleetflow.vehicle.repository.VehicleRepository;
import com.fleetflow.vehicle.repository.VehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleTypeServiceImplTest {

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleTypeMapper vehicleTypeMapper;

    @InjectMocks
    private VehicleTypeServiceImpl service;

    private VehicleType vehicleType;

    private VehicleTypeResponse response;

    @BeforeEach
    void setUp() {

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

        response =
                new VehicleTypeResponse(
                        vehicleType.getId(),
                        vehicleType.getName(),
                        vehicleType.getDescription(),
                        vehicleType.getMaxLoadKg(),
                        null,
                        null
                );
    }

    @Test
    void create_shouldCreateVehicleType() {

        VehicleTypeCreateRequest request =
                new VehicleTypeCreateRequest(
                        "Heavy Truck",
                        "Heavy truck",
                        new BigDecimal("25000")
                );

        when(
                vehicleTypeRepository
                        .existsByNameIgnoreCase(
                                "HEAVY TRUCK"
                        )
        ).thenReturn(false);

        when(
                vehicleTypeMapper.toEntity(request)
        ).thenReturn(vehicleType);

        when(
                vehicleTypeRepository.save(vehicleType)
        ).thenReturn(vehicleType);

        when(
                vehicleTypeMapper.toResponse(vehicleType)
        ).thenReturn(response);

        VehicleTypeResponse result =
                service.create(request);

        assertThat(result)
                .isEqualTo(response);

        verify(
                vehicleTypeRepository
        ).save(vehicleType);
    }

    @Test
    void create_duplicateName_shouldThrowConflict() {

        VehicleTypeCreateRequest request =
                new VehicleTypeCreateRequest(
                        "Heavy Truck",
                        "Heavy truck",
                        new BigDecimal("25000")
                );

        when(
                vehicleTypeRepository
                        .existsByNameIgnoreCase(
                                "HEAVY TRUCK"
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.create(request)
        )
                .isInstanceOf(
                        ConflictException.class
                )
                .hasMessageContaining(
                        "already exists"
                );

        verify(
                vehicleTypeRepository,
                never()
        ).save(any());
    }

    @Test
    void findById_shouldReturnVehicleType() {

        UUID id =
                vehicleType.getId();

        when(
                vehicleTypeRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleTypeMapper.toResponse(
                        vehicleType
                )
        ).thenReturn(response);

        VehicleTypeResponse result =
                service.findById(id);

        assertThat(result)
                .isEqualTo(response);
    }

    @Test
    void findById_notFound_shouldThrow() {

        UUID id =
                UUID.randomUUID();

        when(
                vehicleTypeRepository.findById(id)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.findById(id)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }

    @Test
    void update_shouldUpdateVehicleType() {

        UUID id =
                vehicleType.getId();

        VehicleTypeUpdateRequest request =
                new VehicleTypeUpdateRequest(
                        "LIGHT_TRUCK",
                        "Light truck",
                        new BigDecimal("10000")
                );

        when(
                vehicleTypeRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleTypeRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                "LIGHT_TRUCK",
                                id
                        )
        ).thenReturn(false);

        when(
                vehicleTypeRepository.save(vehicleType)
        ).thenReturn(vehicleType);

        when(
                vehicleTypeMapper.toResponse(
                        vehicleType
                )
        ).thenReturn(response);

        VehicleTypeResponse result =
                service.update(
                        id,
                        request
                );

        assertThat(result)
                .isEqualTo(response);

        verify(
                vehicleTypeMapper
        ).updateEntity(
                vehicleType,
                request
        );
    }

    @Test
    void update_duplicateName_shouldThrowConflict() {

        UUID id =
                vehicleType.getId();

        VehicleTypeUpdateRequest request =
                new VehicleTypeUpdateRequest(
                        "LIGHT_TRUCK",
                        "Light truck",
                        new BigDecimal("10000")
                );

        when(
                vehicleTypeRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleTypeRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                "LIGHT_TRUCK",
                                id
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.update(
                        id,
                        request
                )
        )
                .isInstanceOf(
                        ConflictException.class
                );

        verify(
                vehicleTypeRepository,
                never()
        ).save(any());
    }

    @Test
    void delete_shouldDeleteWhenNotUsed() {

        UUID id =
                vehicleType.getId();

        when(
                vehicleTypeRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByVehicleType_Id(id)
        ).thenReturn(false);

        service.delete(id);

        verify(
                vehicleTypeRepository
        ).delete(vehicleType);
    }

    @Test
    void delete_whenInUse_shouldThrowConflict() {

        UUID id =
                vehicleType.getId();

        when(
                vehicleTypeRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByVehicleType_Id(id)
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.delete(id)
        )
                .isInstanceOf(
                        ConflictException.class
                );

        verify(
                vehicleTypeRepository,
                never()
        ).delete(any());
    }
}