package com.fleetflow.delivery.dto;

import com.fleetflow.delivery.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID id,
        UUID shipmentId,
        DeliveryStatus status,
        OffsetDateTime scheduledAt,
        OffsetDateTime deliveredAt,
        String recipientName,
        String deliveryNotes,
        String proofOfDeliveryUrl
) {
}