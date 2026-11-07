
package com.fleetflow.notification.messaging.event;

import java.util.UUID;

public record InvoiceGeneratedEvent(
        UUID invoiceId,
        UUID customerId,
        String invoiceNumber
) {
}
