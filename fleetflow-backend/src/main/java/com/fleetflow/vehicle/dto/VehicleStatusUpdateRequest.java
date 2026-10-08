package com.fleetflow.vehicle.dto;

import com.fleetflow.vehicle.enums.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record VehicleStatusUpdateRequest(

        @NotNull(
                message = "Vehicle status is required"
        )
        VehicleStatus status
) {
}