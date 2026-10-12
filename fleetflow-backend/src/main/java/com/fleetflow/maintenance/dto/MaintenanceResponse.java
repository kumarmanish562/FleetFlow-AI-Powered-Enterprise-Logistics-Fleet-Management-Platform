package com.fleetflow.maintenance.dto;

import com.fleetflow.maintenance.enums.MaintenanceStatus;
import com.fleetflow.maintenance.enums.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MaintenanceResponse(

        UUID id,
        UUID vehicleId,
        MaintenanceType maintenanceType,
        String description,
        LocalDate maintenanceDate,
        Long mileage,
        BigDecimal cost,
        LocalDate nextServiceDate,
        MaintenanceStatus status,
        boolean safetyCritical,
        String rootCause,
        String partsReplaced,
        BigDecimal downtimeHours,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}