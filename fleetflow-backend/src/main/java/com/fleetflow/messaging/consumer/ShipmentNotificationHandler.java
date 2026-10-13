package com.fleetflow.messaging.consumer;

import com.fleetflow.messaging.event.EventEnvelope;
import com.fleetflow.messaging.event.shipment.ShipmentDeliveredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentNotificationHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ShipmentNotificationHandler.class);

    private static final String CONSUMER =
            "fleetflow-notifications";

    private final ProcessedEventRepository processedEvents;

    public ShipmentNotificationHandler(
            ProcessedEventRepository processedEvents
    ) {
        this.processedEvents = processedEvents;
    }

    @Transactional
    public void handle(
            EventEnvelope<ShipmentDeliveredEvent> event
    ) {
        if (!processedEvents.claim(CONSUMER, event.eventId())) {
            log.info(
                    "Duplicate event ignored eventId={}",
                    event.eventId()
            );
            return;
        }

        ShipmentDeliveredEvent payload = event.payload();

        /*
         * Integrate your existing notification module here.
         *
         * Persist a notification request in the same transaction.
         * Dispatch email/SMS asynchronously after that record commits.
         */
        log.info(
                "Delivery notification request accepted for shipmentId={}",
                payload.shipmentId()
        );
    }
}