package com.fleetflow.maintenance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceCompleteRequest(

        @NotNull
        @PastOrPresent
        LocalDate completedDate,

        @NotNull
        @PositiveOrZero
        Long finalMileage,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal finalCost,

        @NotBlank
        @Size(max = 5000)
        String workPerformed,

        @Size(max = 5000)
        String partsReplaced

) {
}