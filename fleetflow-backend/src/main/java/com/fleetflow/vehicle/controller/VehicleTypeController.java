package com.fleetflow.vehicle.controller;

import com.fleetflow.vehicle.dto.VehicleTypeCreateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeResponse;
import com.fleetflow.vehicle.dto.VehicleTypeUpdateRequest;
import com.fleetflow.vehicle.service.VehicleTypeService;
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
@RequestMapping("/api/v1/vehicle-types")
@RequiredArgsConstructor
public class VehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER')"
    )
    public VehicleTypeResponse create(
            @Valid
            @RequestBody
            VehicleTypeCreateRequest request
    ) {

        return vehicleTypeService.create(
                request
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER')"
    )
    public Page<VehicleTypeResponse> findAll(
            @PageableDefault(
                    size = 20,
                    sort = "name"
            )
            Pageable pageable
    ) {

        return vehicleTypeService.findAll(
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER')"
    )
    public VehicleTypeResponse findById(
            @PathVariable UUID id
    ) {

        return vehicleTypeService.findById(
                id
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER')"
    )
    public VehicleTypeResponse update(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            VehicleTypeUpdateRequest request
    ) {

        return vehicleTypeService.update(
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

        vehicleTypeService.delete(id);
    }
}