package com.fleetflow.driver.repository;

import com.fleetflow.driver.entity.Driver;
import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class DriverRepositoryTest {

    @Autowired
    private DriverRepository driverRepository;

    @Test
    void repositoryLoads() {

        assertNotNull(driverRepository);
    }

    @Test
    void availableDriverQuery_shouldWork() {

        Page<Driver> result =
                driverRepository.findAvailableDrivers(
                        DriverEmploymentStatus.ACTIVE,
                        DriverAvailability.AVAILABLE,
                        LocalDate.now(),
                        PageRequest.of(0, 20)
                );

        assertNotNull(result);
    }
}