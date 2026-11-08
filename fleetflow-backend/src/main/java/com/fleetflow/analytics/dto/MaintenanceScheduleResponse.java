package com.fleetflow.analytics.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceScheduleResponse(
        UUID maintenanceId,
        UUID vehicleId,
        String registrationNumber,
        String maintenanceType,
        String status,
        LocalDate nextServiceDate,
        boolean overdue
) {}
