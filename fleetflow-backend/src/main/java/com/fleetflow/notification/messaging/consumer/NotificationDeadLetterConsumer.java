
package com.fleetflow.notification.messaging.consumer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationDeadLetterConsumer {

    @KafkaListener(
            topics = "${fleetflow.notifications.dead-letter-topic:fleetflow.notifications.dlq}",
            groupId = "${spring.kafka.consumer.group-id:fleetflow-notifications}-dlq"
    )
    public void consume(String payload) {
        // Integrate durable failure storage and operational alerting here.
        // Avoid logging full payloads that might contain personal data.
        log.error(
                "Notification event received by dead-letter consumer; payload length={}",
                payload == null ? 0 : payload.length()
        );
    }
}
