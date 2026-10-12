package com.fleetflow.messaging.outbox;

import java.util.UUID;

public record OutboxClaim(
        UUID id,
        UUID eventId,
        String topic,
        String messageKey,
        String payload,
        String eventType,
        String aggregateId,
        int attempts,
        UUID claimToken
) {
}