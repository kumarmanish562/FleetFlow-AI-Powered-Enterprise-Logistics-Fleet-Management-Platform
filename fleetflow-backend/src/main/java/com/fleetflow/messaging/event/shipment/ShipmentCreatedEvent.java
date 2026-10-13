package com.fleetflow.messaging.event.shipment;

import java.time.Instant;

public record ShipmentCreatedEvent(
        String shipmentId,
        String trackingNumber,
        Instant createdAt
) {
}