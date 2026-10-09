package com.fleetflow.driver.controller;

import com.fleetflow.driver.dto.DriverAvailabilityUpdateRequest;
import com.fleetflow.driver.dto.DriverCreateRequest;
import com.fleetflow.driver.dto.DriverResponse;
import com.fleetflow.driver.dto.DriverStatusUpdateRequest;
import com.fleetflow.driver.dto.DriverSummaryResponse;
import com.fleetflow.driver.dto.DriverUpdateRequest;
import com.fleetflow.driver.service.DriverService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER')"
    )
    public ResponseEntity<DriverResponse> createDriver(
            @Valid
            @RequestBody
            DriverCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        driverService.createDriver(request)
                );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DISPATCHER')"
    )
    public ResponseEntity<
            Page<DriverSummaryResponse>
            > getDrivers(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String employmentStatus,

            @RequestParam(required = false)
            String availability,

            @RequestParam(required = false)
            String licenseType,

            @PageableDefault(
                    size = 20,
                    sort = "lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                driverService.getDrivers(
                        search,
                        employmentStatus,
                        availability,
                        licenseType,
                        pageable
                )
        );
    }

    @GetMapping("/{driverId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DISPATCHER')"
    )
    public ResponseEntity<DriverResponse> getDriver(
            @PathVariable UUID driverId
    ) {

        return ResponseEntity.ok(
                driverService.getDriver(driverId)
        );
    }

    @PutMapping("/{driverId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER')"
    )
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable UUID driverId,

            @Valid
            @RequestBody
            DriverUpdateRequest request
    ) {

        return ResponseEntity.ok(
                driverService.updateDriver(
                        driverId,
                        request
                )
        );
    }

    @PatchMapping("/{driverId}/status")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER')"
    )
    public ResponseEntity<DriverResponse>
    updateEmploymentStatus(

            @PathVariable UUID driverId,

            @Valid
            @RequestBody
            DriverStatusUpdateRequest request
    ) {

        return ResponseEntity.ok(
                driverService.updateEmploymentStatus(
                        driverId,
                        request
                )
        );
    }

    @PatchMapping("/{driverId}/availability")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DRIVER')"
    )
    public ResponseEntity<DriverResponse>
    updateAvailability(

            @PathVariable UUID driverId,

            @Valid
            @RequestBody
            DriverAvailabilityUpdateRequest request
    ) {

        return ResponseEntity.ok(
                driverService.updateAvailability(
                        driverId,
                        request
                )
        );
    }

    @PostMapping(
            "/{driverId}/assign-vehicle/{vehicleId}"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER')"
    )
    public ResponseEntity<DriverResponse>
    assignVehicle(

            @PathVariable UUID driverId,

            @PathVariable UUID vehicleId
    ) {

        return ResponseEntity.ok(
                driverService.assignVehicle(
                        driverId,
                        vehicleId
                )
        );
    }

    @DeleteMapping("/{driverId}/vehicle")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER')"
    )
    public ResponseEntity<DriverResponse>
    unassignVehicle(

            @PathVariable UUID driverId
    ) {

        return ResponseEntity.ok(
                driverService.unassignVehicle(
                        driverId
                )
        );
    }

    @GetMapping("/available")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DISPATCHER')"
    )
    public ResponseEntity<
            Page<DriverSummaryResponse>
            > getAvailableDrivers(

            @PageableDefault(
                    size = 20,
                    sort = "lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                driverService.getAvailableDrivers(
                        pageable
                )
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse>
    getCurrentDriver() {

        return ResponseEntity.ok(
                driverService.getCurrentDriver()
        );
    }

    @DeleteMapping("/{driverId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'FLEET_MANAGER')"
    )
    public ResponseEntity<Void> deactivateDriver(
            @PathVariable UUID driverId
    ) {

        driverService.deactivateDriver(
                driverId
        );

        return ResponseEntity.noContent().build();
    }
}