package com.fleetflow.vehicle.service;


import com.fleetflow.common.exception.BadRequestException;
import com.fleetflow.common.exception.ConflictException;
import com.fleetflow.common.exception.ResourceNotFoundException;
import com.fleetflow.vehicle.dto.VehicleCreateRequest;
import com.fleetflow.vehicle.dto.VehicleFilter;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleStatusUpdateRequest;
import com.fleetflow.vehicle.dto.VehicleUpdateRequest;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.mapper.VehicleMapper;
import com.fleetflow.vehicle.repository.VehicleRepository;
import com.fleetflow.vehicle.repository.VehicleSpecification;
import com.fleetflow.vehicle.repository.VehicleTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import com.fleetflow.user.service.CurrentOrganizationProvider;
import com.fleetflow.messaging.outbox.OutboxService;
import com.fleetflow.messaging.event.vehicle.VehicleStatusChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Transactional(readOnly = true)
public class VehicleServiceImpl
        implements VehicleService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "registrationNumber",
                    "manufacturer",
                    "model",
                    "manufactureYear",
                    "fuelType",
                    "status",
                    "currentOdometer",
                    "createdAt",
                    "updatedAt"
            );

    private final VehicleRepository vehicleRepository;

    private final VehicleTypeRepository vehicleTypeRepository;

    private final VehicleMapper vehicleMapper;

    private final VehicleStatusTransitionValidator statusTransitionValidator;
    private final CurrentOrganizationProvider organizations;
    private final OutboxService outbox;

    @Autowired
    public VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleTypeRepository vehicleTypeRepository,
                              VehicleMapper vehicleMapper, VehicleStatusTransitionValidator statusTransitionValidator,
                              CurrentOrganizationProvider organizations, OutboxService outbox) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleMapper = vehicleMapper;
        this.statusTransitionValidator = statusTransitionValidator;
        this.organizations = organizations;
        this.outbox = outbox;
    }

    public VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleTypeRepository vehicleTypeRepository,
                              VehicleMapper vehicleMapper, VehicleStatusTransitionValidator statusTransitionValidator,
                              CurrentOrganizationProvider organizations) {
        this(vehicleRepository, vehicleTypeRepository, vehicleMapper, statusTransitionValidator, organizations, null);
    }

    public VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleTypeRepository vehicleTypeRepository,
                              VehicleMapper vehicleMapper, VehicleStatusTransitionValidator statusTransitionValidator) {
        this(vehicleRepository, vehicleTypeRepository, vehicleMapper, statusTransitionValidator, null);
    }

    @Override
    @Transactional
    public VehicleResponse create(
            VehicleCreateRequest request
    ) {

        VehicleType vehicleType =
                findVehicleType(
                        request.vehicleTypeId()
                );

        String registration =
                normalizeRegistration(
                        request.registrationNumber()
                );

        if (
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCase(
                                registration
                        )
        ) {

            throw new ConflictException(
                    "Vehicle registration already exists: "
                            + registration
            );
        }

        String vin =
                normalizeVin(request.vin());

        if (
                vin != null
                        && vehicleRepository
                        .existsByVinIgnoreCase(vin)
        ) {

            throw new ConflictException(
                    "Vehicle VIN already exists: "
                            + vin
            );
        }

        Vehicle vehicle =
                vehicleMapper.toEntity(
                        request,
                        vehicleType
                );
        if (organizations != null) vehicle.setOrganizationId(organizations.requireOrganizationId());

        Vehicle saved =
                vehicleRepository.save(vehicle);

        return vehicleMapper.toResponse(saved);
    }

    @Override
    public Page<VehicleResponse> findAll(
            VehicleFilter filter,
            Pageable pageable
    ) {

        Pageable safePageable =
                validateAndNormalizePageable(
                        pageable
                );

        Specification<Vehicle> specification =
                Specification
                        .where(
                                VehicleSpecification.search(
                                        filter.search()
                                )
                        )
                        .and(
                                VehicleSpecification.hasVehicleType(
                                        filter.vehicleTypeId()
                                )
                        )
                        .and(
                                VehicleSpecification.hasStatus(
                                        filter.status()
                                )
                        )
                        .and(
                                VehicleSpecification.hasFuelType(
                                        filter.fuelType()
                                )
                        )
                        .and(
                                VehicleSpecification.hasManufacturer(
                                        filter.manufacturer()
                                )
                        )
                        .and(
                                VehicleSpecification.hasModel(
                                        filter.model()
                                )
                        );

        return vehicleRepository
                .findAll(
                        specification,
                        safePageable
                )
                .map(vehicleMapper::toResponse);
    }

    @Override
    public VehicleResponse findById(
            UUID id
    ) {

        Vehicle vehicle =
                findVehicle(id);

        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    public VehicleResponse findOperationalById(UUID id, UUID organizationId) {
        return vehicleRepository.findByIdAndOrganizationId(id, organizationId)
                .map(vehicleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    @Override
    public Page<VehicleResponse> findOperationalByStatus(VehicleStatus status, UUID organizationId, Pageable pageable) {
        Pageable safe = validateAndNormalizePageable(pageable);
        return vehicleRepository.findByOrganizationIdAndStatus(organizationId, status, safe)
                .map(vehicleMapper::toResponse);
    }

    @Override
    @Transactional
    public VehicleResponse update(
            UUID id,
            VehicleUpdateRequest request
    ) {

        Vehicle vehicle =
                findVehicle(id);

        VehicleType vehicleType =
                findVehicleType(
                        request.vehicleTypeId()
                );

        String registration =
                normalizeRegistration(
                        request.registrationNumber()
                );

        if (
                vehicleRepository
                        .existsByRegistrationNumberIgnoreCaseAndIdNot(
                                registration,
                                id
                        )
        ) {

            throw new ConflictException(
                    "Vehicle registration already exists: "
                            + registration
            );
        }

        String vin =
                normalizeVin(request.vin());

        if (
                vin != null
                        && vehicleRepository
                        .existsByVinIgnoreCaseAndIdNot(
                                vin,
                                id
                        )
        ) {

            throw new ConflictException(
                    "Vehicle VIN already exists: "
                            + vin
            );
        }

        validateOdometer(
                vehicle.getCurrentOdometer(),
                request.currentOdometer()
        );

        vehicleMapper.updateEntity(
                vehicle,
                request,
                vehicleType
        );

        Vehicle updated =
                vehicleRepository.save(vehicle);

        return vehicleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(
            UUID id
    ) {

        Vehicle vehicle =
                findVehicle(id);

        vehicleRepository.delete(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse updateStatus(
            UUID id,
            VehicleStatusUpdateRequest request
    ) {

        Vehicle vehicle =
                findVehicle(id);

        var previousStatus = vehicle.getStatus();
        statusTransitionValidator.validate(
                vehicle.getStatus(),
                request.status()
        );

        vehicle.setStatus(
                request.status()
        );

        Vehicle saved =
                vehicleRepository.save(vehicle);

        if (outbox != null && previousStatus != saved.getStatus())
            outbox.record(outbox.vehicleTopic(), "VehicleStatusChanged", "Vehicle", saved.getId().toString(), null,
                    new VehicleStatusChangedEvent(saved.getId(), previousStatus.name(), saved.getStatus().name(),
                            java.time.Instant.now()));

        return vehicleMapper.toResponse(saved);
    }

    private Vehicle findVehicle(
            UUID id
    ) {

        return vehicleRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Vehicle not found: "
                                                + id
                                )
                );
    }

    private VehicleType findVehicleType(
            UUID id
    ) {

        return vehicleTypeRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Vehicle type not found: "
                                                + id
                                )
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

    private void validateOdometer(
            BigDecimal current,
            BigDecimal requested
    ) {

        if (
                current != null
                        && requested != null
                        && requested.compareTo(current) < 0
        ) {

            throw new BadRequestException(
                    "Odometer cannot decrease. "
                            + "Current value: "
                            + current
            );
        }
    }

    private Pageable validateAndNormalizePageable(
            Pageable pageable
    ) {

        int page =
                pageable.getPageNumber();

        if (page < 0) {

            throw new BadRequestException(
                    "Page number cannot be negative"
            );
        }

        int requestedSize =
                pageable.getPageSize();

        if (requestedSize <= 0) {

            throw new BadRequestException(
                    "Page size must be greater than zero"
            );
        }

        if (requestedSize > MAX_PAGE_SIZE) {

            throw new BadRequestException(
                    "Page size cannot exceed "
                            + MAX_PAGE_SIZE
            );
        }

        for (
                Sort.Order order :
                pageable.getSort()
        ) {

            if (
                    !ALLOWED_SORT_FIELDS.contains(
                            order.getProperty()
                    )
            ) {

                throw new BadRequestException(
                        "Sorting by field '"
                                + order.getProperty()
                                + "' is not allowed"
                );
            }
        }

        return PageRequest.of(
                page,
                requestedSize,
                pageable.getSort()
        );
    }
}
