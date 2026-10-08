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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleTypeServiceImpl
        implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;

    private final VehicleRepository vehicleRepository;

    private final VehicleTypeMapper vehicleTypeMapper;

    @Override
    @Transactional
    public VehicleTypeResponse create(
            VehicleTypeCreateRequest request
    ) {

        String name =
                normalizeName(request.name());

        if (
                vehicleTypeRepository
                        .existsByNameIgnoreCase(name)
        ) {

            throw new ConflictException(
                    "Vehicle type already exists: " + name
            );
        }

        VehicleType vehicleType =
                vehicleTypeMapper.toEntity(request);

        VehicleType saved =
                vehicleTypeRepository.save(vehicleType);

        return vehicleTypeMapper.toResponse(saved);
    }

    @Override
    public Page<VehicleTypeResponse> findAll(
            Pageable pageable
    ) {

        return vehicleTypeRepository
                .findAll(pageable)
                .map(vehicleTypeMapper::toResponse);
    }

    @Override
    public VehicleTypeResponse findById(
            UUID id
    ) {

        VehicleType vehicleType =
                findEntity(id);

        return vehicleTypeMapper.toResponse(
                vehicleType
        );
    }

    @Override
    @Transactional
    public VehicleTypeResponse update(
            UUID id,
            VehicleTypeUpdateRequest request
    ) {

        VehicleType vehicleType =
                findEntity(id);

        String name =
                normalizeName(request.name());

        if (
                vehicleTypeRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                name,
                                id
                        )
        ) {

            throw new ConflictException(
                    "Vehicle type already exists: " + name
            );
        }

        vehicleTypeMapper.updateEntity(
                vehicleType,
                request
        );

        VehicleType updated =
                vehicleTypeRepository.save(vehicleType);

        return vehicleTypeMapper.toResponse(
                updated
        );
    }

    @Override
    @Transactional
    public void delete(
            UUID id
    ) {

        VehicleType vehicleType =
                findEntity(id);

        boolean inUse =
                vehicleRepository
                        .existsByVehicleType_Id(id);

        if (inUse) {

            throw new ConflictException(
                    "Vehicle type cannot be deleted because "
                            + "vehicles are using it"
            );
        }

        vehicleTypeRepository.delete(vehicleType);
    }

    private VehicleType findEntity(
            UUID id
    ) {

        return vehicleTypeRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Vehicle type not found: " + id
                        )
                );
    }

    private String normalizeName(
            String name
    ) {

        return name
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();
    }
}