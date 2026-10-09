package com.fleetflow.driver.service;

import com.fleetflow.common.exception.DriverBusinessException;
import com.fleetflow.common.exception.DriverNotFoundException;
import com.fleetflow.common.exception.DuplicateDriverException;
import com.fleetflow.common.exception.VehicleAssignmentException;
import com.fleetflow.driver.dto.DriverAvailabilityUpdateRequest;
import com.fleetflow.driver.dto.DriverCreateRequest;
import com.fleetflow.driver.dto.DriverResponse;
import com.fleetflow.driver.dto.DriverStatusUpdateRequest;
import com.fleetflow.driver.dto.DriverSummaryResponse;
import com.fleetflow.driver.dto.DriverUpdateRequest;
import com.fleetflow.driver.entity.Driver;
import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;
import com.fleetflow.driver.enums.LicenseType;
import com.fleetflow.driver.mapper.DriverMapper;
import com.fleetflow.driver.repository.DriverRepository;
import com.fleetflow.driver.specification.DriverSpecification;
import com.fleetflow.user.entity.User;
import com.fleetflow.user.repository.UserRepository;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DriverServiceImpl
        implements DriverService {

    private final DriverRepository driverRepository;

    private final UserRepository userRepository;

    private final VehicleRepository vehicleRepository;

    private final DriverMapper driverMapper;

    @Override
    @Transactional
    public DriverResponse createDriver(
            DriverCreateRequest request
    ) {

        log.info(
                "Creating driver for user {}",
                request.userId()
        );

        if (driverRepository.existsByUserId(
                request.userId()
        )) {

            throw new DuplicateDriverException(
                    "Driver already exists for user: "
                            + request.userId()
            );
        }

        if (driverRepository
                .existsByLicenseNumberIgnoreCase(
                        request.licenseNumber()
                )) {

            throw new DuplicateDriverException(
                    "License number already exists: "
                            + request.licenseNumber()
            );
        }

        User user =
                userRepository.findById(
                        request.userId()
                ).orElseThrow(
                        () -> new DriverBusinessException(
                                "User not found: "
                                        + request.userId()
                        )
                );

        if (request.licenseExpiryDate()
                .isBefore(LocalDate.now())) {

            throw new DriverBusinessException(
                    "Driver license is already expired"
            );
        }

        Driver driver =
                Driver.builder()

                        .user(user)

                        .firstName(
                                request.firstName()
                        )

                        .lastName(
                                request.lastName()
                        )

                        .phone(
                                request.phone()
                        )

                        .dateOfBirth(
                                request.dateOfBirth()
                        )

                        .licenseNumber(
                                request.licenseNumber()
                        )

                        .licenseExpiryDate(
                                request.licenseExpiryDate()
                        )

                        .licenseType(
                                request.licenseType()
                        )

                        .yearsOfExperience(
                                request.yearsOfExperience()
                        )

                        .employmentStatus(
                                DriverEmploymentStatus.ACTIVE
                        )

                        .availability(
                                DriverAvailability.OFF_DUTY
                        )

                        .hireDate(
                                request.hireDate()
                        )

                        .build();

        Driver saved =
                driverRepository.save(driver);

        log.info(
                "Driver created successfully: {}",
                saved.getId()
        );

        return driverMapper.toResponse(saved);
    }

    @Override
    public DriverResponse getDriver(
            UUID driverId
    ) {

        Driver driver =
                getDriverEntity(driverId);

        return driverMapper.toResponse(driver);
    }

    @Override
    public Page<DriverSummaryResponse> getDrivers(
            String search,
            String employmentStatus,
            String availability,
            String licenseType,
            Pageable pageable
    ) {

        DriverEmploymentStatus status =
                parseEmploymentStatus(
                        employmentStatus
                );

        DriverAvailability driverAvailability =
                parseAvailability(
                        availability
                );

        LicenseType driverLicenseType =
                parseLicenseType(
                        licenseType
                );

        return driverRepository.findAll(
                        DriverSpecification.filter(
                                search,
                                status,
                                driverAvailability,
                                driverLicenseType
                        ),
                        pageable
                )
                .map(driverMapper::toSummary);
    }

    @Override
    @Transactional
    public DriverResponse updateDriver(
            UUID driverId,
            DriverUpdateRequest request
    ) {

        Driver driver =
                getDriverEntity(driverId);

        if (!driver.getLicenseNumber()
                .equalsIgnoreCase(
                        request.licenseNumber()
                )
                &&
                driverRepository
                        .existsByLicenseNumberIgnoreCase(
                                request.licenseNumber()
                        )) {

            throw new DuplicateDriverException(
                    "License number already exists"
            );
        }

        if (request.licenseExpiryDate()
                .isBefore(LocalDate.now())) {

            throw new DriverBusinessException(
                    "License expiry date cannot be in the past"
            );
        }

        driver.setFirstName(
                request.firstName()
        );

        driver.setLastName(
                request.lastName()
        );

        driver.setPhone(
                request.phone()
        );

        driver.setDateOfBirth(
                request.dateOfBirth()
        );

        driver.setLicenseNumber(
                request.licenseNumber()
        );

        driver.setLicenseType(
                request.licenseType()
        );

        driver.setLicenseExpiryDate(
                request.licenseExpiryDate()
        );

        driver.setYearsOfExperience(
                request.yearsOfExperience()
        );

        driver.setHireDate(
                request.hireDate()
        );

        return driverMapper.toResponse(
                driverRepository.save(driver)
        );
    }

    @Override
    @Transactional
    public DriverResponse updateEmploymentStatus(
            UUID driverId,
            DriverStatusUpdateRequest request
    ) {

        Driver driver =
                getDriverEntity(driverId);

        DriverEmploymentStatus current =
                driver.getEmploymentStatus();

        DriverEmploymentStatus target =
                request.employmentStatus();

        validateStatusTransition(
                current,
                target
        );

        driver.setEmploymentStatus(target);

        if (target ==
                DriverEmploymentStatus.TERMINATED) {

            driver.setAvailability(
                    DriverAvailability.OFF_DUTY
            );

            if (driver.getAssignedVehicle() != null) {

                Vehicle vehicle =
                        driver.getAssignedVehicle();

                driver.setAssignedVehicle(null);

                if (vehicle.getStatus()
                        == VehicleStatus.ASSIGNED) {

                    vehicle.setStatus(
                            VehicleStatus.AVAILABLE
                    );
                }
            }
        }

        return driverMapper.toResponse(
                driverRepository.save(driver)
        );
    }

    @Override
    @Transactional
    public DriverResponse updateAvailability(
            UUID driverId,
            DriverAvailabilityUpdateRequest request
    ) {

        Driver driver =
                getDriverEntity(driverId);

        validateAvailabilityTransition(
                driver,
                request.availability()
        );

        driver.setAvailability(
                request.availability()
        );

        return driverMapper.toResponse(
                driverRepository.save(driver)
        );
    }

    @Override
    @Transactional
    public DriverResponse assignVehicle(
            UUID driverId,
            UUID vehicleId
    ) {

        Driver driver =
                getDriverEntity(driverId);

        if (driver.getEmploymentStatus()
                != DriverEmploymentStatus.ACTIVE) {

            throw new VehicleAssignmentException(
                    "Only ACTIVE drivers can be assigned vehicles"
            );
        }

        if (driver.getAvailability()
                != DriverAvailability.AVAILABLE) {

            throw new VehicleAssignmentException(
                    "Driver must be AVAILABLE"
            );
        }

        if (driver.getLicenseExpiryDate()
                .isBefore(LocalDate.now())) {

            throw new VehicleAssignmentException(
                    "Driver license has expired"
            );
        }

        if (driver.getAssignedVehicle() != null) {

            throw new VehicleAssignmentException(
                    "Driver already has an assigned vehicle"
            );
        }

        Vehicle vehicle =
                vehicleRepository.findById(
                        vehicleId
                ).orElseThrow(
                        () -> new VehicleAssignmentException(
                                "Vehicle not found: "
                                        + vehicleId
                        )
                );

        if (vehicle.getStatus()
                != VehicleStatus.AVAILABLE) {

            throw new VehicleAssignmentException(
                    "Vehicle is not available. Current status: "
                            + vehicle.getStatus()
            );
        }

        driver.setAssignedVehicle(vehicle);

        vehicle.setStatus(
                VehicleStatus.ASSIGNED
        );

        Driver saved =
                driverRepository.save(driver);

        log.info(
                "Vehicle {} assigned to driver {}",
                vehicleId,
                driverId
        );

        return driverMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DriverResponse unassignVehicle(
            UUID driverId
    ) {

        Driver driver =
                getDriverEntity(driverId);

        Vehicle vehicle =
                driver.getAssignedVehicle();

        if (vehicle == null) {

            throw new VehicleAssignmentException(
                    "Driver has no assigned vehicle"
            );
        }

        driver.setAssignedVehicle(null);

        if (vehicle.getStatus()
                == VehicleStatus.ASSIGNED) {

            vehicle.setStatus(
                    VehicleStatus.AVAILABLE
            );
        }

        Driver saved =
                driverRepository.save(driver);

        log.info(
                "Vehicle {} unassigned from driver {}",
                vehicle.getId(),
                driverId
        );

        return driverMapper.toResponse(saved);
    }

    @Override
    public Page<DriverSummaryResponse> getAvailableDrivers(
            Pageable pageable
    ) {

        return driverRepository
                .findAvailableDrivers(
                        DriverEmploymentStatus.ACTIVE,
                        DriverAvailability.AVAILABLE,
                        LocalDate.now(),
                        pageable
                )
                .map(driverMapper::toSummary);
    }

    @Override
    public DriverResponse getCurrentDriver() {

        /*
         * Replace the user lookup below with the exact
         * authentication helper already used by your
         * Phase 3 security implementation.
         */

        throw new UnsupportedOperationException(
                "Connect this method to the existing Phase 3 authenticated-user resolver"
        );
    }

    @Override
    @Transactional
    public void deactivateDriver(
            UUID driverId
    ) {

        Driver driver =
                getDriverEntity(driverId);

        if (driver.getEmploymentStatus()
                == DriverEmploymentStatus.TERMINATED) {

            return;
        }

        driver.setEmploymentStatus(
                DriverEmploymentStatus.INACTIVE
        );

        driver.setAvailability(
                DriverAvailability.OFF_DUTY
        );

        driverRepository.save(driver);
    }

    private Driver getDriverEntity(
            UUID driverId
    ) {

        return driverRepository
                .findById(driverId)
                .orElseThrow(
                        () -> new DriverNotFoundException(
                                driverId
                        )
                );
    }

    private void validateStatusTransition(
            DriverEmploymentStatus current,
            DriverEmploymentStatus target
    ) {

        if (current == target) {
            return;
        }

        if (current ==
                DriverEmploymentStatus.TERMINATED) {

            throw new DriverBusinessException(
                    "TERMINATED driver cannot change status"
            );
        }

        if (current ==
                DriverEmploymentStatus.SUSPENDED
                &&
                target != DriverEmploymentStatus.ACTIVE
                &&
                target != DriverEmploymentStatus.TERMINATED) {

            throw new DriverBusinessException(
                    "Invalid status transition: "
                            + current
                            + " -> "
                            + target
            );
        }
    }

    private void validateAvailabilityTransition(
            Driver driver,
            DriverAvailability target
    ) {

        if (driver.getEmploymentStatus()
                != DriverEmploymentStatus.ACTIVE) {

            if (target != DriverAvailability.OFF_DUTY
                    &&
                    target != DriverAvailability.ON_LEAVE) {

                throw new DriverBusinessException(
                        "Inactive, suspended or terminated drivers "
                                + "cannot become operational"
                );
            }
        }

        if (driver.getEmploymentStatus()
                == DriverEmploymentStatus.TERMINATED) {

            throw new DriverBusinessException(
                    "Terminated driver cannot change availability"
            );
        }

        if (target ==
                DriverAvailability.ON_DELIVERY
                &&
                driver.getAssignedVehicle() == null) {

            throw new DriverBusinessException(
                    "Driver must have an assigned vehicle "
                            + "before going ON_DELIVERY"
            );
        }
    }

    private DriverEmploymentStatus parseEmploymentStatus(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return DriverEmploymentStatus.valueOf(
                    value.toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new DriverBusinessException(
                    "Invalid employment status: "
                            + value
            );
        }
    }

    private DriverAvailability parseAvailability(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return DriverAvailability.valueOf(
                    value.toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new DriverBusinessException(
                    "Invalid availability: "
                            + value
            );
        }
    }

    private LicenseType parseLicenseType(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LicenseType.valueOf(
                    value.toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new DriverBusinessException(
                    "Invalid license type: "
                            + value
            );
        }
    }
}