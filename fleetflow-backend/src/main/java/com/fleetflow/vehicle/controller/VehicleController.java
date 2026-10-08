package com.fleetflow.vehicle.controller;

import com.fleetflow.vehicle.dto.VehicleCreateRequest;
import com.fleetflow.vehicle.dto.VehicleFilter;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleStatusUpdateRequest;
import com.fleetflow.vehicle.dto.VehicleUpdateRequest;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER')"
    )
    public VehicleResponse create(
            @Valid
            @RequestBody
            VehicleCreateRequest request
    ) {

        return vehicleService.create(
                request
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER')"
    )
    public Page<VehicleResponse> findAll(

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    required = false
            )
            UUID vehicleTypeId,

            @RequestParam(
                    required = false
            )
            VehicleStatus status,

            @RequestParam(
                    required = false
            )
            FuelType fuelType,

            @RequestParam(
                    required = false
            )
            String manufacturer,

            @RequestParam(
                    required = false
            )
            String model,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable
    ) {

        VehicleFilter filter =
                new VehicleFilter(
                        search,
                        vehicleTypeId,
                        status,
                        fuelType,
                        manufacturer,
                        model
                );

        return vehicleService.findAll(
                filter,
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER')"
    )
    public VehicleResponse findById(
            @PathVariable UUID id
    ) {

        return vehicleService.findById(
                id
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER')"
    )
    public VehicleResponse update(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            VehicleUpdateRequest request
    ) {

        return vehicleService.update(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN')"
    )
    public void delete(
            @PathVariable UUID id
    ) {

        vehicleService.delete(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER')"
    )
    public VehicleResponse updateStatus(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            VehicleStatusUpdateRequest request
    ) {

        return vehicleService.updateStatus(
                id,
                request
        );
    }
}