package com.fleetflow.vehicle.service;

import com.fleetflow.vehicle.dto.VehicleTypeCreateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeResponse;
import com.fleetflow.vehicle.dto.VehicleTypeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleTypeService {

    VehicleTypeResponse create(
            VehicleTypeCreateRequest request
    );

    Page<VehicleTypeResponse> findAll(
            Pageable pageable
    );

    VehicleTypeResponse findById(
            UUID id
    );

    VehicleTypeResponse update(
            UUID id,
            VehicleTypeUpdateRequest request
    );

    void delete(
            UUID id
    );
}