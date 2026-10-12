package com.fleetflow.maintenance.controller;

import com.fleetflow.maintenance.dto.MaintenanceCompleteRequest;
import com.fleetflow.maintenance.dto.MaintenanceCreateRequest;
import com.fleetflow.maintenance.dto.MaintenanceResponse;
import com.fleetflow.maintenance.dto.MaintenanceUpdateRequest;
import com.fleetflow.maintenance.service.MaintenanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping("/vehicles/{vehicleId}/maintenance-records")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public MaintenanceResponse create(
            @PathVariable("vehicleId") UUID vehicleId,
            @Valid @RequestBody MaintenanceCreateRequest request) {

        return maintenanceService.create(vehicleId, request);
    }

    @GetMapping("/vehicles/{vehicleId}/maintenance-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public Page<MaintenanceResponse> getVehicleHistory(
            @PathVariable("vehicleId") UUID vehicleId,
            Pageable pageable) {

        return maintenanceService.getVehicleHistory(vehicleId, pageable);
    }

    @GetMapping("/maintenance-records/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public MaintenanceResponse getById(
            @PathVariable("id") UUID id) {

        return maintenanceService.getById(id);
    }

    @PatchMapping("/maintenance-records/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public MaintenanceResponse update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody MaintenanceUpdateRequest request) {

        return maintenanceService.update(id, request);
    }

    @PatchMapping("/maintenance-records/{id}/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public MaintenanceResponse start(
            @PathVariable("id") UUID id) {

        return maintenanceService.start(id);
    }

    @PatchMapping("/maintenance-records/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public MaintenanceResponse complete(
            @PathVariable("id") UUID id,
            @Valid @RequestBody MaintenanceCompleteRequest request) {

        return maintenanceService.complete(id, request);
    }

    @PatchMapping("/maintenance-records/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public MaintenanceResponse cancel(
            @PathVariable("id") UUID id) {

        return maintenanceService.cancel(id);
    }

    @GetMapping("/maintenance-records/upcoming")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public List<MaintenanceResponse> upcoming(
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return maintenanceService.getUpcoming(startDate, endDate);
    }

    @GetMapping("/maintenance-records/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public List<MaintenanceResponse> overdue() {
        return maintenanceService.getOverdue(LocalDate.now());
    }
}