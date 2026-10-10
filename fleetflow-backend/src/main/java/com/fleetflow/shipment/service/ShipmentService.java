package com.fleetflow.shipment.service;

import com.fleetflow.shipment.dto.ShipmentAssignmentRequest;
import com.fleetflow.shipment.dto.ShipmentCreateRequest;
import com.fleetflow.shipment.dto.ShipmentResponse;
import com.fleetflow.shipment.dto.ShipmentStatusUpdateRequest;
import com.fleetflow.shipment.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShipmentService {
    ShipmentResponse create(ShipmentCreateRequest request);
    ShipmentResponse getById(UUID id);
    ShipmentResponse getByShipmentNumber(String shipmentNumber);
    Page<ShipmentResponse> list(ShipmentStatus status, UUID customerId, Pageable pageable);
    ShipmentResponse confirm(UUID id);
    ShipmentResponse assign(UUID id, ShipmentAssignmentRequest request);
    ShipmentResponse updateStatus(UUID id, ShipmentStatusUpdateRequest request);
    ShipmentResponse cancel(UUID id);
}
