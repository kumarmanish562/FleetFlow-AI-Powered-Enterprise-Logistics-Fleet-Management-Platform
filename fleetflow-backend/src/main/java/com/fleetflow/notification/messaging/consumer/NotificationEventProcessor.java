
package com.fleetflow.notification.messaging.consumer;

import com.fleetflow.notification.enums.NotificationEventType;
import com.fleetflow.notification.messaging.event.NotificationEventEnvelope;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationEventProcessor {

    private static final Set<NotificationEventType> SUPPORTED_EVENTS =
            EnumSet.allOf(NotificationEventType.class);

    public void process(NotificationEventEnvelope<?> event) {
        NotificationEventType eventType;

        try {
            eventType = NotificationEventType.valueOf(
                    event.eventType()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported notification event type: "
                            + event.eventType(),
                    exception
            );
        }

        if (!SUPPORTED_EVENTS.contains(eventType)) {
            throw new IllegalArgumentException(
                    "Unsupported notification event type: " + eventType
            );
        }

        switch (eventType) {
            case SHIPMENT_CREATED ->
                    processShipmentCreated(event);
            case SHIPMENT_PICKED_UP ->
                    processShipmentPickedUp(event);
            case SHIPMENT_DELAYED ->
                    processShipmentDelayed(event);
            case SHIPMENT_DELIVERED ->
                    processShipmentDelivered(event);
            case INVOICE_GENERATED ->
                    processInvoiceGenerated(event);
            case PAYMENT_RECEIVED ->
                    processPaymentReceived(event);
        }
    }

    private void processShipmentCreated(
            NotificationEventEnvelope<?> event
    ) {
        // Connect to shipment notification creation.
        throw notImplemented(event);
    }

    private void processShipmentPickedUp(
            NotificationEventEnvelope<?> event
    ) {
        // Connect to pickup notification creation.
        throw notImplemented(event);
    }

    private void processShipmentDelayed(
            NotificationEventEnvelope<?> event
    ) {
        // Connect to delay notification creation.
        throw notImplemented(event);
    }

    private void processShipmentDelivered(
            NotificationEventEnvelope<?> event
    ) {
        // Connect to delivery notification creation.
        throw notImplemented(event);
    }

    private void processInvoiceGenerated(
            NotificationEventEnvelope<?> event
    ) {
        // Connect to billing notification creation.
        throw notImplemented(event);
    }

    private void processPaymentReceived(
            NotificationEventEnvelope<?> event
    ) {
        // Connect to verified payment notification creation.
        throw notImplemented(event);
    }

    private UnsupportedOperationException notImplemented(
            NotificationEventEnvelope<?> event
    ) {
        return new UnsupportedOperationException(
                "Notification handler is not connected for event "
                        + event.eventType()
        );
    }
}
