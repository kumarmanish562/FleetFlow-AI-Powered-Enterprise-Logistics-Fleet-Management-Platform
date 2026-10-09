package com.fleetflow.driver.mapper;

import com.fleetflow.driver.dto.DriverResponse;
import com.fleetflow.driver.dto.DriverSummaryResponse;
import com.fleetflow.driver.entity.Driver;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DriverMapper {

    private static final int LICENSE_WARNING_DAYS = 30;

    public DriverResponse toResponse(
            Driver driver
    ) {

        LocalDate today =
                LocalDate.now();

        LocalDate expiry =
                driver.getLicenseExpiryDate();

        boolean expired =
                expiry.isBefore(today);

        boolean expiringSoon =
                !expired &&
                        !expiry.isAfter(
                                today.plusDays(
                                        LICENSE_WARNING_DAYS
                                )
                        );

        return new DriverResponse(

                driver.getId(),

                driver.getUser().getId(),

                driver.getFirstName(),

                driver.getLastName(),

                driver.getPhone(),

                driver.getDateOfBirth(),

                driver.getLicenseNumber(),

                driver.getLicenseType(),

                driver.getLicenseExpiryDate(),

                driver.getYearsOfExperience(),

                driver.getEmploymentStatus(),

                driver.getAvailability(),

                driver.getAssignedVehicle() != null
                        ? driver.getAssignedVehicle().getId()
                        : null,

                driver.getHireDate(),

                expired,

                expiringSoon
        );
    }

    public DriverSummaryResponse toSummary(
            Driver driver
    ) {

        return new DriverSummaryResponse(

                driver.getId(),

                driver.getFirstName(),

                driver.getLastName(),

                driver.getLicenseNumber(),

                driver.getEmploymentStatus(),

                driver.getAvailability(),

                driver.getAssignedVehicle() != null
                        ? driver.getAssignedVehicle().getId()
                        : null
        );
    }
}