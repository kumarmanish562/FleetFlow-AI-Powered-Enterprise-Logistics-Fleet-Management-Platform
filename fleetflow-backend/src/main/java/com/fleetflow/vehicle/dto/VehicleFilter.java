package com.fleetflow.vehicle.dto;

import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;

import java.util.UUID;

public record VehicleFilter(

        String search,

        UUID vehicleTypeId,

        VehicleStatus status,

        FuelType fuelType,

        String manufacturer,

        String model
) {
}