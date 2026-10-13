package com.fleetflow.messaging.event.shipment;

import java.time.Instant;

public record ShipmentPickedUpEvent(
        String shipmentId,
        Instant pickedUpAt
) {
}