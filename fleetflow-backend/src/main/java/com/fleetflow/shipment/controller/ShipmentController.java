package com.fleetflow.shipment.controller;

import com.fleetflow.shipment.dto.ShipmentAssignmentRequest;
import com.fleetflow.shipment.dto.ShipmentCreateRequest;
import com.fleetflow.shipment.dto.ShipmentResponse;
import com.fleetflow.shipment.dto.ShipmentStatusUpdateRequest;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public ShipmentResponse create(@Valid @RequestBody ShipmentCreateRequest request) {
        return shipmentService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public ShipmentResponse getById(@PathVariable UUID id) {
        return shipmentService.getById(id);
    }

    @GetMapping("/number/{shipmentNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public ShipmentResponse getByShipmentNumber(@PathVariable String shipmentNumber) {
        return shipmentService.getByShipmentNumber(shipmentNumber);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public Page<ShipmentResponse> list(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) UUID customerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return shipmentService.list(status, customerId, pageable);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public ShipmentResponse confirm(@PathVariable UUID id) {
        return shipmentService.confirm(id);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public ShipmentResponse assign(
            @PathVariable UUID id,
            @Valid @RequestBody ShipmentAssignmentRequest request) {
        return shipmentService.assign(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public ShipmentResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return shipmentService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    public ShipmentResponse cancel(@PathVariable UUID id) {
        return shipmentService.cancel(id);
    }
}
