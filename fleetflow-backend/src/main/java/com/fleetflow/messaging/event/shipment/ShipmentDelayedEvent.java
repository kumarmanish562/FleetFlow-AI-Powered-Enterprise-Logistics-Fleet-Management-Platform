package com.fleetflow.messaging.event.shipment;

import java.time.Instant;

public record ShipmentDelayedEvent(
        String shipmentId,
        String reasonCode,
        Instant expectedDeliveryAt,
        Instant delayedAt
) {
}