package com.fleetflow.messaging.consumer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.messaging.event.EventEnvelope;
import com.fleetflow.messaging.event.shipment.ShipmentDeliveredEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentDeliveredListener {

    private final ObjectMapper objectMapper;
    private final ShipmentNotificationHandler handler;

    public ShipmentDeliveredListener(
            ObjectMapper objectMapper,
            ShipmentNotificationHandler handler
    ) {
        this.objectMapper = objectMapper;
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${fleetflow.messaging.topics.shipment:fleetflow.shipment.events.v1}",
            groupId = "fleetflow-notifications"
    )
    public void onMessage(
            ConsumerRecord<String, String> record
    ) throws Exception {

        JavaType type = objectMapper.getTypeFactory()
                .constructParametricType(
                        EventEnvelope.class,
                        ShipmentDeliveredEvent.class
                );

        EventEnvelope<ShipmentDeliveredEvent> event =
                objectMapper.readValue(record.value(), type);

        if (!"ShipmentDelivered".equals(event.eventType())) {
            return;
        }

        handler.handle(event);
    }
}
