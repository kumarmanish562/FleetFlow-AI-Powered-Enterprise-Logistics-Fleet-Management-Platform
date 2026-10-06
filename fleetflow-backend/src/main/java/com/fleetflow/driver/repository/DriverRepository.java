package com.fleetflow.driver.repository;

import com.fleetflow.driver.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository
        extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByLicenseNumber(
            String licenseNumber
    );

    Optional<Driver> findByUserId(UUID userId);

    boolean existsByLicenseNumber(
            String licenseNumber
    );
}