package com.fleetflow.vehicle.service;

import com.fleetflow.common.exception.ConflictException;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleStatusTransitionValidatorTest {

    private VehicleStatusTransitionValidator validator;

    @BeforeEach
    void setUp() {

        validator =
                new VehicleStatusTransitionValidator();
    }

    @Test
    void sameStatus_shouldBeAllowed() {

        assertThatCode(() ->
                validator.validate(
                        VehicleStatus.AVAILABLE,
                        VehicleStatus.AVAILABLE
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void availableToAssigned_shouldBeAllowed() {

        assertThatCode(() ->
                validator.validate(
                        VehicleStatus.AVAILABLE,
                        VehicleStatus.ASSIGNED
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void assignedToInTransit_shouldBeAllowed() {

        assertThatCode(() ->
                validator.validate(
                        VehicleStatus.ASSIGNED,
                        VehicleStatus.IN_TRANSIT
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void inTransitToAvailable_shouldBeAllowed() {

        assertThatCode(() ->
                validator.validate(
                        VehicleStatus.IN_TRANSIT,
                        VehicleStatus.AVAILABLE
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void availableToMaintenance_shouldBeAllowed() {

        assertThatCode(() ->
                validator.validate(
                        VehicleStatus.AVAILABLE,
                        VehicleStatus.MAINTENANCE
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void maintenanceToAvailable_shouldBeAllowed() {

        assertThatCode(() ->
                validator.validate(
                        VehicleStatus.MAINTENANCE,
                        VehicleStatus.AVAILABLE
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void availableToRetired_shouldBeAllowed() {

        assertThatCode(() ->
                validator.validate(
                        VehicleStatus.AVAILABLE,
                        VehicleStatus.RETIRED
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void retiredToAvailable_shouldBeRejected() {

        assertThatThrownBy(() ->
                validator.validate(
                        VehicleStatus.RETIRED,
                        VehicleStatus.AVAILABLE
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(
                        "Invalid vehicle status transition"
                );
    }

    @Test
    void availableToInTransit_shouldBeRejected() {

        assertThatThrownBy(() ->
                validator.validate(
                        VehicleStatus.AVAILABLE,
                        VehicleStatus.IN_TRANSIT
                )
        )
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void retiredToAssigned_shouldBeRejected() {

        assertThatThrownBy(() ->
                validator.validate(
                        VehicleStatus.RETIRED,
                        VehicleStatus.ASSIGNED
                )
        )
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void inTransitToAssigned_shouldBeRejected() {

        assertThatThrownBy(() ->
                validator.validate(
                        VehicleStatus.IN_TRANSIT,
                        VehicleStatus.ASSIGNED
                )
        )
                .isInstanceOf(ConflictException.class);
    }
}