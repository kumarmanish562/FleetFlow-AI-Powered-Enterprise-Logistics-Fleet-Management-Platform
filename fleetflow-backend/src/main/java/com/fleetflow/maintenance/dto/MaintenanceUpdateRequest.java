package com.fleetflow.maintenance.dto;

import com.fleetflow.maintenance.enums.MaintenanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceUpdateRequest(

        MaintenanceType maintenanceType,

        @Size(min = 1, max = 5000)
        String description,

        @FutureOrPresent
        LocalDate maintenanceDate,

        @PositiveOrZero
        Long mileage,

        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal cost,

        @FutureOrPresent
        LocalDate nextServiceDate,

        Boolean safetyCritical,

        @Size(max = 5000)
        String rootCause,

        @Size(max = 5000)
        String partsReplaced,

        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal downtimeHours

) {
}