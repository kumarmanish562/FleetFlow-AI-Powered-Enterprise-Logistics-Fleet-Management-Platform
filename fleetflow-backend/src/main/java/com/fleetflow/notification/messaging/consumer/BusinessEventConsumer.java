
package com.fleetflow.notification.messaging.consumer;

import com.fleetflow.notification.messaging.event.NotificationEventEnvelope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessEventConsumer {

    private final NotificationEventProcessor eventProcessor;

    @KafkaListener(
            topics = "${fleetflow.messaging.notification-topic:fleetflow.business.events}",
            groupId = "${spring.kafka.consumer.group-id:fleetflow-notifications}"
    )
    public void consume(NotificationEventEnvelope<?> event) {
        if (event == null
                || event.eventId() == null
                || event.eventType() == null
                || event.payload() == null) {

            throw new IllegalArgumentException(
                    "Invalid notification event envelope"
            );
        }

        log.debug(
                "Received notification event: eventId={}, eventType={}",
                event.eventId(),
                event.eventType()
        );

        eventProcessor.process(event);
    }
}
