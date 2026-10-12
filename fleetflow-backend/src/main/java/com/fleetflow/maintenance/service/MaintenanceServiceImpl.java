package com.fleetflow.maintenance.service;

import com.fleetflow.maintenance.dto.MaintenanceCompleteRequest;
import com.fleetflow.maintenance.dto.MaintenanceCreateRequest;
import com.fleetflow.maintenance.dto.MaintenanceResponse;
import com.fleetflow.maintenance.dto.MaintenanceUpdateRequest;
import com.fleetflow.maintenance.entity.MaintenanceRecord;
import com.fleetflow.maintenance.enums.MaintenanceStatus;
import com.fleetflow.maintenance.mapper.MaintenanceMapper;
import com.fleetflow.maintenance.repository.MaintenanceRecordRepository;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceMapper maintenanceMapper;

    @Override
    public MaintenanceResponse create(
            UUID vehicleId,
            MaintenanceCreateRequest request) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found: " + vehicleId
                ));

        validateNextServiceDate(
                request.maintenanceDate(),
                request.nextServiceDate()
        );

        validateMileage(
                vehicleId,
                request.mileage(),
                request.maintenanceDate()
        );

        MaintenanceRecord record =
                maintenanceMapper.toEntity(request, vehicle);

        return maintenanceMapper.toResponse(
                maintenanceRepository.save(record)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceResponse getById(UUID id) {
        return maintenanceMapper.toResponse(findRecord(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceResponse> getVehicleHistory(
            UUID vehicleId,
            Pageable pageable) {

        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vehicle not found: " + vehicleId
            );
        }

        return maintenanceRepository
                .findByVehicleId(vehicleId, pageable)
                .map(maintenanceMapper::toResponse);
    }

    @Override
    public MaintenanceResponse update(
            UUID id,
            MaintenanceUpdateRequest request) {

        MaintenanceRecord record = findRecord(id);

        if (record.getStatus() == MaintenanceStatus.COMPLETED
                || record.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed or cancelled records cannot be edited"
            );
        }

        if (request.maintenanceType() != null) {
            record.setMaintenanceType(request.maintenanceType());
        }

        if (request.description() != null) {
            if (request.description().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Description must not be blank"
                );
            }

            record.setDescription(request.description().trim());
        }

        if (request.maintenanceDate() != null) {
            record.setMaintenanceDate(request.maintenanceDate());
        }

        if (request.mileage() != null) {
            record.setMileage(request.mileage());
        }

        if (request.cost() != null) {
            record.setCost(request.cost());
        }

        if (request.nextServiceDate() != null) {
            record.setNextServiceDate(request.nextServiceDate());
        }

        if (request.safetyCritical() != null) {
            record.setSafetyCritical(request.safetyCritical());
        }

        if (request.rootCause() != null) {
            record.setRootCause(trimToNull(request.rootCause()));
        }

        if (request.partsReplaced() != null) {
            record.setPartsReplaced(trimToNull(request.partsReplaced()));
        }

        if (request.downtimeHours() != null) {
            record.setDowntimeHours(request.downtimeHours());
        }

        validateNextServiceDate(
                record.getMaintenanceDate(),
                record.getNextServiceDate()
        );

        validateMileage(
                record.getVehicle().getId(),
                record.getMileage(),
                record.getMaintenanceDate()
        );

        return maintenanceMapper.toResponse(record);
    }

    @Override
    public MaintenanceResponse start(UUID id) {

        MaintenanceRecord record = findRecord(id);

        requireStatus(record, MaintenanceStatus.SCHEDULED);

        record.setStatus(MaintenanceStatus.IN_PROGRESS);

        return maintenanceMapper.toResponse(record);
    }

    @Override
    public MaintenanceResponse complete(
            UUID id,
            MaintenanceCompleteRequest request) {

        MaintenanceRecord record = findRecord(id);

        requireStatus(record, MaintenanceStatus.IN_PROGRESS);

        validateMileage(
                record.getVehicle().getId(),
                request.finalMileage(),
                request.completedDate()
        );

        record.setMaintenanceDate(request.completedDate());
        record.setMileage(request.finalMileage());
        record.setCost(request.finalCost());

        // This model uses description for the work performed at completion.
        record.setDescription(request.workPerformed().trim());

        if (request.partsReplaced() != null) {
            record.setPartsReplaced(trimToNull(request.partsReplaced()));
        }

        record.setStatus(MaintenanceStatus.COMPLETED);

        return maintenanceMapper.toResponse(record);
    }

    @Override
    public MaintenanceResponse cancel(UUID id) {

        MaintenanceRecord record = findRecord(id);

        if (record.getStatus() == MaintenanceStatus.COMPLETED
                || record.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This maintenance record cannot be cancelled"
            );
        }

        record.setStatus(MaintenanceStatus.CANCELLED);

        return maintenanceMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getUpcoming(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null
                || endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid upcoming maintenance date range"
            );
        }

        return maintenanceRepository
                .findByStatusAndMaintenanceDateBetween(
                        MaintenanceStatus.SCHEDULED,
                        startDate,
                        endDate
                )
                .stream()
                .map(maintenanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getOverdue(LocalDate today) {

        if (today == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current date is required"
            );
        }

        return maintenanceRepository
                .findByStatusInAndMaintenanceDateBefore(
                        List.of(
                                MaintenanceStatus.SCHEDULED,
                                MaintenanceStatus.IN_PROGRESS
                        ),
                        today
                )
                .stream()
                .map(maintenanceMapper::toResponse)
                .toList();
    }

    private MaintenanceRecord findRecord(UUID id) {

        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maintenance record ID is required"
            );
        }

        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Maintenance record not found: " + id
                ));
    }

    private void requireStatus(
            MaintenanceRecord record,
            MaintenanceStatus expected) {

        if (record.getStatus() != expected) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Expected status " + expected
                            + " but found " + record.getStatus()
            );
        }
    }

    private void validateNextServiceDate(
            LocalDate maintenanceDate,
            LocalDate nextServiceDate) {

        if (nextServiceDate != null
                && nextServiceDate.isBefore(maintenanceDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Next service date cannot be before maintenance date"
            );
        }
    }

    private void validateMileage(
            UUID vehicleId,
            Long mileage,
            LocalDate maintenanceDate) {

        if (mileage == null || mileage < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mileage must be non-negative"
            );
        }

        if (maintenanceDate == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maintenance date is required"
            );
        }

        List<MaintenanceRecord> completedRecords =
                maintenanceRepository
                        .findByVehicleIdOrderByMaintenanceDateDesc(vehicleId)
                        .stream()
                        .filter(record ->
                                record.getStatus() == MaintenanceStatus.COMPLETED)
                        .toList();

        completedRecords.stream()
                .filter(record ->
                        !record.getMaintenanceDate().isAfter(maintenanceDate))
                .max(Comparator.comparing(
                        MaintenanceRecord::getMaintenanceDate))
                .ifPresent(previous -> {
                    if (mileage < previous.getMileage()) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Mileage cannot be lower than the previous "
                                        + "completed maintenance reading"
                        );
                    }
                });

        completedRecords.stream()
                .filter(record ->
                        record.getMaintenanceDate().isAfter(maintenanceDate))
                .min(Comparator.comparing(
                        MaintenanceRecord::getMaintenanceDate))
                .ifPresent(next -> {
                    if (mileage > next.getMileage()) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Mileage cannot exceed a later completed "
                                        + "maintenance reading"
                        );
                    }
                });
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}