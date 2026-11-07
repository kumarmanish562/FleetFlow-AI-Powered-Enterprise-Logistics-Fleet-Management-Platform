
package com.fleetflow.notification.messaging.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationEventEnvelope<T>(
        UUID eventId,
        String eventType,
        OffsetDateTime occurredAt,
        T payload
) {
}
