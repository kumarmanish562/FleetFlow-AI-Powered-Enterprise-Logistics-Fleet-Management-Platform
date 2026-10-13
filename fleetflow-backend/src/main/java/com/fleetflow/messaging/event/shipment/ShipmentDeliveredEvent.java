package com.fleetflow.messaging.event.shipment;

import java.time.Instant;

public record ShipmentDeliveredEvent(
        String shipmentId,
        String trackingNumber,
        Instant deliveredAt
) {
}