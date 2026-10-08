package com.fleetflow.vehicle.service;

import com.fleetflow.vehicle.dto.VehicleCreateRequest;
import com.fleetflow.vehicle.dto.VehicleFilter;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleStatusUpdateRequest;
import com.fleetflow.vehicle.dto.VehicleUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {

    VehicleResponse create(
            VehicleCreateRequest request
    );

    Page<VehicleResponse> findAll(
            VehicleFilter filter,
            Pageable pageable
    );

    VehicleResponse findById(
            UUID id
    );

    VehicleResponse update(
            UUID id,
            VehicleUpdateRequest request
    );

    void delete(
            UUID id
    );

    VehicleResponse updateStatus(
            UUID id,
            VehicleStatusUpdateRequest request
    );
}