package com.fleetflow.shipment.dto;

import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        String shipmentNumber,
        UUID customerId,
        UUID driverId,
        UUID vehicleId,
        UUID routeId,
        ShipmentStatus status,
        ShipmentPriority priority,
        String originAddress,
        String destinationAddress,
        BigDecimal totalWeightKg,
        BigDecimal declaredValue,
        OffsetDateTime pickupDate,
        OffsetDateTime expectedDeliveryDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
