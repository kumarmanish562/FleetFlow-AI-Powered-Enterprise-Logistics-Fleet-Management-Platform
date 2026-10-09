package com.fleetflow.driver.repository;

import com.fleetflow.driver.entity.Driver;
import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;
import com.fleetflow.driver.enums.LicenseType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository
        extends JpaRepository<Driver, UUID>,
        JpaSpecificationExecutor<Driver> {

    Optional<Driver> findByLicenseNumberIgnoreCase(
            String licenseNumber
    );

    Optional<Driver> findByUserId(
            UUID userId
    );

    boolean existsByLicenseNumberIgnoreCase(
            String licenseNumber
    );

    boolean existsByUserId(
            UUID userId
    );

    Page<Driver> findByEmploymentStatus(
            DriverEmploymentStatus employmentStatus,
            Pageable pageable
    );

    Page<Driver> findByAvailability(
            DriverAvailability availability,
            Pageable pageable
    );

    Page<Driver> findByLicenseType(
            LicenseType licenseType,
            Pageable pageable
    );

    @Query("""
        SELECT d
        FROM Driver d
        WHERE d.employmentStatus = :status
          AND d.availability = :availability
          AND d.licenseExpiryDate >= :today
        """)
    Page<Driver> findAvailableDrivers(
            @Param("status")
            DriverEmploymentStatus status,

            @Param("availability")
            DriverAvailability availability,

            @Param("today")
            LocalDate today,

            Pageable pageable
    );
}