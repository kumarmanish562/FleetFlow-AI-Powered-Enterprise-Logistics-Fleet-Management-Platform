package com.fleetflow.vehicle.service;

import com.fleetflow.common.exception.BadRequestException;
import com.fleetflow.common.exception.ConflictException;
import com.fleetflow.common.exception.ResourceNotFoundException;
import com.fleetflow.vehicle.dto.VehicleCreateRequest;
import com.fleetflow.vehicle.dto.VehicleFilter;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleStatusUpdateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeSummary;
import com.fleetflow.vehicle.dto.VehicleUpdateRequest;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.mapper.VehicleMapper;
import com.fleetflow.vehicle.repository.VehicleRepository;
import com.fleetflow.vehicle.repository.VehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    private VehicleStatusTransitionValidator
            statusTransitionValidator;

    private VehicleServiceImpl service;

    private VehicleType vehicleType;

    private Vehicle vehicle;

    private VehicleResponse response;

    @BeforeEach
    void setUp() {

        statusTransitionValidator =
                new VehicleStatusTransitionValidator();

        service =
                new VehicleServiceImpl(
                        vehicleRepository,
                        vehicleTypeRepository,
                        vehicleMapper,
                        statusTransitionValidator
                );

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

        vehicle =
                Vehicle.builder()
                        .vehicleType(vehicleType)
                        .registrationNumber(
                                "CG04AB1234"
                        )
                        .vin(
                                "1HGBH41JXMN109186"
                        )
                        .manufacturer("Tata")
                        .model("Prima 3530")
                        .manufactureYear(2024)
                        .fuelType(FuelType.DIESEL)
                        .status(VehicleStatus.AVAILABLE)
                        .currentOdometer(
                                new BigDecimal("1000")
                        )
                        .build();

        vehicle.setId(
                UUID.randomUUID()
        );

        response =
                new VehicleResponse(
                        vehicle.getId(),
                        new VehicleTypeSummary(
                                vehicleType.getId(),
                                vehicleType.getName(),
                                vehicleType.getMaxLoadKg()
                        ),
                        vehicle.getRegistrationNumber(),
                        vehicle.getVin(),
                        vehicle.getManufacturer(),
                        vehicle.getModel(),
                        vehicle.getManufactureYear(),
                        vehicle.getFuelType(),
                        vehicle.getStatus(),
                        vehicle.getCurrentOdometer(),
                        null,
                        null
                );
    }

    @Test
    void create_shouldCreateVehicle() {

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        vehicleType.getId(),
                        "CG04AB1234",
                        "1HGBH41JXMN109186",
                        "Tata",
                        "Prima 3530",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("1000")
                );

        when(
                vehicleTypeRepository.findById(
                        vehicleType.getId()
                )
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCase(
                                "CG04AB1234"
                        )
        ).thenReturn(false);

        when(
                vehicleRepository
                        .existsByVinIgnoreCase(
                                "1HGBH41JXMN109186"
                        )
        ).thenReturn(false);

        when(
                vehicleMapper.toEntity(
                        request,
                        vehicleType
                )
        ).thenReturn(vehicle);

        when(
                vehicleRepository.save(vehicle)
        ).thenReturn(vehicle);

        when(
                vehicleMapper.toResponse(vehicle)
        ).thenReturn(response);

        VehicleResponse result =
                service.create(request);

        assertThat(result)
                .isEqualTo(response);

        verify(
                vehicleRepository
        ).save(vehicle);
    }

    @Test
    void create_vehicleTypeNotFound_shouldThrow() {

        UUID typeId =
                UUID.randomUUID();

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        typeId,
                        "CG04AB1234",
                        null,
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        BigDecimal.ZERO
                );

        when(
                vehicleTypeRepository.findById(typeId)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.create(request)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );

        verify(
                vehicleRepository,
                never()
        ).save(any());
    }

    @Test
    void create_duplicateRegistration_shouldThrow() {

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        vehicleType.getId(),
                        "CG04AB1234",
                        null,
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        BigDecimal.ZERO
                );

        when(
                vehicleTypeRepository.findById(
                        vehicleType.getId()
                )
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCase(
                                "CG04AB1234"
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.create(request)
        )
                .isInstanceOf(
                        ConflictException.class
                );

        verify(
                vehicleRepository,
                never()
        ).save(any());
    }

    @Test
    void create_duplicateVin_shouldThrow() {

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        vehicleType.getId(),
                        "CG04NEW9999",
                        "1HGBH41JXMN109186",
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        BigDecimal.ZERO
                );

        when(
                vehicleTypeRepository.findById(
                        vehicleType.getId()
                )
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCase(
                                "CG04NEW9999"
                        )
        ).thenReturn(false);

        when(
                vehicleRepository
                        .existsByVinIgnoreCase(
                                "1HGBH41JXMN109186"
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.create(request)
        )
                .isInstanceOf(
                        ConflictException.class
                );

        verify(
                vehicleRepository,
                never()
        ).save(any());
    }

    @Test
    void findById_shouldReturnVehicle() {

        UUID id =
                vehicle.getId();

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        when(
                vehicleMapper.toResponse(vehicle)
        ).thenReturn(response);

        VehicleResponse result =
                service.findById(id);

        assertThat(result)
                .isEqualTo(response);
    }

    @Test
    void findById_notFound_shouldThrow() {

        UUID id =
                UUID.randomUUID();

        when(
                vehicleRepository.findById(id)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.findById(id)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }

    @Test
    void update_shouldUpdateVehicle() {

        UUID id =
                vehicle.getId();

        VehicleUpdateRequest request =
                new VehicleUpdateRequest(
                        vehicleType.getId(),
                        "CG04AB1234",
                        "1HGBH41JXMN109186",
                        "Tata Motors",
                        "Prima 3530",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("1500")
                );

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        when(
                vehicleTypeRepository.findById(
                        vehicleType.getId()
                )
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCaseAndIdNot(
                                "CG04AB1234",
                                id
                        )
        ).thenReturn(false);

        when(
                vehicleRepository
                        .existsByVinIgnoreCaseAndIdNot(
                                "1HGBH41JXMN109186",
                                id
                        )
        ).thenReturn(false);

        when(
                vehicleRepository.save(vehicle)
        ).thenReturn(vehicle);

        when(
                vehicleMapper.toResponse(vehicle)
        ).thenReturn(response);

        VehicleResponse result =
                service.update(
                        id,
                        request
                );

        assertThat(result)
                .isEqualTo(response);

        verify(
                vehicleMapper
        ).updateEntity(
                vehicle,
                request,
                vehicleType
        );
    }

    @Test
    void update_duplicateRegistration_shouldThrow() {

        UUID id =
                vehicle.getId();

        VehicleUpdateRequest request =
                new VehicleUpdateRequest(
                        vehicleType.getId(),
                        "CG04DUPLICATE",
                        null,
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("1500")
                );

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        when(
                vehicleTypeRepository.findById(
                        vehicleType.getId()
                )
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCaseAndIdNot(
                                "CG04DUPLICATE",
                                id
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () ->
                        service.update(
                                id,
                                request
                        )
        )
                .isInstanceOf(
                        ConflictException.class
                );

        verify(
                vehicleRepository,
                never()
        ).save(any());
    }

    @Test
    void update_duplicateVin_shouldThrow() {

        UUID id =
                vehicle.getId();

        VehicleUpdateRequest request =
                new VehicleUpdateRequest(
                        vehicleType.getId(),
                        "CG04NEW",
                        "DUPLICATEVIN",
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("1500")
                );

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        when(
                vehicleTypeRepository.findById(
                        vehicleType.getId()
                )
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCaseAndIdNot(
                                "CG04NEW",
                                id
                        )
        ).thenReturn(false);

        when(
                vehicleRepository
                        .existsByVinIgnoreCaseAndIdNot(
                                "DUPLICATEVIN",
                                id
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () ->
                        service.update(
                                id,
                                request
                        )
        )
                .isInstanceOf(
                        ConflictException.class
                );

        verify(
                vehicleRepository,
                never()
        ).save(any());
    }

    @Test
    void update_lowerOdometer_shouldThrow() {

        UUID id =
                vehicle.getId();

        VehicleUpdateRequest request =
                new VehicleUpdateRequest(
                        vehicleType.getId(),
                        "CG04AB1234",
                        null,
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("500")
                );

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        when(
                vehicleTypeRepository.findById(
                        vehicleType.getId()
                )
        ).thenReturn(
                Optional.of(vehicleType)
        );

        when(
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCaseAndIdNot(
                                "CG04AB1234",
                                id
                        )
        ).thenReturn(false);

        assertThatThrownBy(
                () ->
                        service.update(
                                id,
                                request
                        )
        )
                .isInstanceOf(
                        BadRequestException.class
                );

        verify(
                vehicleRepository,
                never()
        ).save(any());
    }

    @Test
    void delete_shouldDeleteVehicle() {

        UUID id =
                vehicle.getId();

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        service.delete(id);

        verify(
                vehicleRepository
        ).delete(vehicle);
    }

    @Test
    void updateStatus_shouldChangeStatus() {

        UUID id =
                vehicle.getId();

        VehicleStatusUpdateRequest request =
                new VehicleStatusUpdateRequest(
                        VehicleStatus.ASSIGNED
                );

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        when(
                vehicleRepository.save(vehicle)
        ).thenReturn(vehicle);

        when(
                vehicleMapper.toResponse(vehicle)
        ).thenReturn(response);

        VehicleResponse result =
                service.updateStatus(
                        id,
                        request
                );

        assertThat(
                vehicle.getStatus()
        ).isEqualTo(
                VehicleStatus.ASSIGNED
        );

        assertThat(result)
                .isEqualTo(response);

        verify(
                vehicleRepository
        ).save(vehicle);
    }

    @Test
    void updateStatus_invalidTransition_shouldThrow() {

        UUID id =
                vehicle.getId();

        vehicle.setStatus(
                VehicleStatus.RETIRED
        );

        VehicleStatusUpdateRequest request =
                new VehicleStatusUpdateRequest(
                        VehicleStatus.AVAILABLE
                );

        when(
                vehicleRepository.findById(id)
        ).thenReturn(
                Optional.of(vehicle)
        );

        assertThatThrownBy(
                () ->
                        service.updateStatus(
                                id,
                                request
                        )
        )
                .isInstanceOf(
                        ConflictException.class
                );

        verify(
                vehicleRepository,
                never()
        ).save(any());
    }

    @Test
    void findAll_shouldReturnPage() {

        VehicleFilter filter =
                new VehicleFilter(
                        "Tata",
                        vehicleType.getId(),
                        VehicleStatus.AVAILABLE,
                        FuelType.DIESEL,
                        "Tata",
                        "Prima"
                );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        when(
                vehicleRepository.findAll(
                        any(org.springframework.data.jpa.domain.Specification.class),
                        any(PageRequest.class)
                )
        ).thenReturn(
                new PageImpl<>(
                        List.of(vehicle)
                )
        );

        when(
                vehicleMapper.toResponse(vehicle)
        ).thenReturn(response);

        var result =
                service.findAll(
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().get(0))
                .isEqualTo(response);
    }

    @Test
    void findAll_invalidPageSize_shouldThrow() {

        VehicleFilter filter =
                new VehicleFilter(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        101
                );

        assertThatThrownBy(
                () ->
                        service.findAll(
                                filter,
                                pageable
                        )
        )
                .isInstanceOf(
                        BadRequestException.class
                )
                .hasMessageContaining(
                        "cannot exceed"
                );
    }

    @Test
    void findAll_invalidSortField_shouldThrow() {

        VehicleFilter filter =
                new VehicleFilter(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        PageRequest pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by("password")
                );

        assertThatThrownBy(
                () ->
                        service.findAll(
                                filter,
                                pageable
                        )
        )
                .isInstanceOf(
                        BadRequestException.class
                )
                .hasMessageContaining(
                        "Sorting by field"
                );
    }
}