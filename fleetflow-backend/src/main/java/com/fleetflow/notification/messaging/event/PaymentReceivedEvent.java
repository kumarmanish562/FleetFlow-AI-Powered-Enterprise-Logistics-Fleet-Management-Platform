
package com.fleetflow.notification.messaging.event;

import java.util.UUID;

public record PaymentReceivedEvent(
        UUID paymentId,
        UUID customerId,
        String paymentReference
) {
}
