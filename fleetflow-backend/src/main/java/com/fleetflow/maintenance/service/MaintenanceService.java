package com.fleetflow.maintenance.service;

import com.fleetflow.maintenance.dto.MaintenanceCompleteRequest;
import com.fleetflow.maintenance.dto.MaintenanceCreateRequest;
import com.fleetflow.maintenance.dto.MaintenanceResponse;
import com.fleetflow.maintenance.dto.MaintenanceUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MaintenanceService {

    MaintenanceResponse create(
            UUID vehicleId,
            MaintenanceCreateRequest request
    );

    MaintenanceResponse getById(UUID id);

    Page<MaintenanceResponse> getVehicleHistory(
            UUID vehicleId,
            Pageable pageable
    );

    MaintenanceResponse update(
            UUID id,
            MaintenanceUpdateRequest request
    );

    MaintenanceResponse start(UUID id);

    MaintenanceResponse complete(
            UUID id,
            MaintenanceCompleteRequest request
    );

    MaintenanceResponse cancel(UUID id);

    List<MaintenanceResponse> getUpcoming(
            LocalDate startDate,
            LocalDate endDate
    );

    List<MaintenanceResponse> getOverdue(LocalDate today);
}