package com.fleetflow.vehicle.service;


import com.fleetflow.common.exception.ConflictException;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.springframework.stereotype.Component;

@Component
public class VehicleStatusTransitionValidator {

    public void validate(
            VehicleStatus current,
            VehicleStatus target
    ) {

        if (current == target) {
            return;
        }

        boolean valid =
                switch (current) {

                    case AVAILABLE ->
                            target == VehicleStatus.ASSIGNED
                                    || target == VehicleStatus.MAINTENANCE
                                    || target == VehicleStatus.OUT_OF_SERVICE
                                    || target == VehicleStatus.RETIRED;

                    case ASSIGNED ->
                            target == VehicleStatus.IN_TRANSIT
                                    || target == VehicleStatus.AVAILABLE
                                    || target == VehicleStatus.MAINTENANCE;

                    case IN_TRANSIT ->
                            target == VehicleStatus.AVAILABLE
                                    || target == VehicleStatus.MAINTENANCE;

                    case MAINTENANCE ->
                            target == VehicleStatus.AVAILABLE
                                    || target == VehicleStatus.OUT_OF_SERVICE;

                    case OUT_OF_SERVICE ->
                            target == VehicleStatus.MAINTENANCE
                                    || target == VehicleStatus.RETIRED;

                    case RETIRED ->
                            false;
                };

        if (!valid) {

            throw new ConflictException(
                    "Invalid vehicle status transition from "
                            + current
                            + " to "
                            + target
            );
        }
    }
}