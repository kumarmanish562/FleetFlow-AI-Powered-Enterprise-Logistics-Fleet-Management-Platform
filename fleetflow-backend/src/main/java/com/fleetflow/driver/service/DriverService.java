package com.fleetflow.driver.service;

import com.fleetflow.driver.dto.DriverAvailabilityUpdateRequest;
import com.fleetflow.driver.dto.DriverCreateRequest;
import com.fleetflow.driver.dto.DriverResponse;
import com.fleetflow.driver.dto.DriverStatusUpdateRequest;
import com.fleetflow.driver.dto.DriverSummaryResponse;
import com.fleetflow.driver.dto.DriverUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DriverService {

    DriverResponse createDriver(
            DriverCreateRequest request
    );

    DriverResponse getDriver(
            UUID driverId
    );

    DriverResponse getOperationalDriver(UUID driverId, UUID organizationId);

    Page<DriverSummaryResponse> getOperationalAvailableDrivers(UUID organizationId, Pageable pageable);

    Page<DriverSummaryResponse> getDrivers(
            String search,
            String employmentStatus,
            String availability,
            String licenseType,
            Pageable pageable
    );

    DriverResponse updateDriver(
            UUID driverId,
            DriverUpdateRequest request
    );

    DriverResponse updateEmploymentStatus(
            UUID driverId,
            DriverStatusUpdateRequest request
    );

    DriverResponse updateAvailability(
            UUID driverId,
            DriverAvailabilityUpdateRequest request
    );

    DriverResponse assignVehicle(
            UUID driverId,
            UUID vehicleId
    );

    DriverResponse unassignVehicle(
            UUID driverId
    );

    Page<DriverSummaryResponse> getAvailableDrivers(
            Pageable pageable
    );

    DriverResponse getCurrentDriver();

    void deactivateDriver(
            UUID driverId
    );
}
