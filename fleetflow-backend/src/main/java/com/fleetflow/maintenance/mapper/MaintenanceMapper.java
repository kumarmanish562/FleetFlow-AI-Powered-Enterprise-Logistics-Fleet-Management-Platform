package com.fleetflow.maintenance.mapper;

import com.fleetflow.maintenance.dto.MaintenanceCreateRequest;
import com.fleetflow.maintenance.dto.MaintenanceResponse;
import com.fleetflow.maintenance.entity.MaintenanceRecord;
import com.fleetflow.maintenance.enums.MaintenanceStatus;
import com.fleetflow.vehicle.entity.Vehicle;

import org.springframework.stereotype.Component;

@Component
public class MaintenanceMapper {

    public MaintenanceRecord toEntity(
            MaintenanceCreateRequest request,
            Vehicle vehicle) {

        MaintenanceRecord record = new MaintenanceRecord();

        record.setVehicle(vehicle);
        record.setMaintenanceType(request.maintenanceType());
        record.setDescription(request.description().trim());
        record.setMaintenanceDate(request.maintenanceDate());
        record.setMileage(request.mileage());
        record.setCost(request.cost());
        record.setNextServiceDate(request.nextServiceDate());
        record.setSafetyCritical(request.safetyCritical());
        record.setRootCause(trimToNull(request.rootCause()));
        record.setPartsReplaced(trimToNull(request.partsReplaced()));
        record.setDowntimeHours(request.downtimeHours());
        record.setStatus(MaintenanceStatus.SCHEDULED);

        return record;
    }

    public MaintenanceResponse toResponse(MaintenanceRecord record) {

        return new MaintenanceResponse(
                record.getId(),
                record.getVehicle().getId(),
                record.getMaintenanceType(),
                record.getDescription(),
                record.getMaintenanceDate(),
                record.getMileage(),
                record.getCost(),
                record.getNextServiceDate(),
                record.getStatus(),
                record.isSafetyCritical(),
                record.getRootCause(),
                record.getPartsReplaced(),
                record.getDowntimeHours(),
                record.getVersion(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}