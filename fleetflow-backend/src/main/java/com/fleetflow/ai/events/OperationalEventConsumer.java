package com.fleetflow.ai.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.ai.service.AIEventContextService;
import com.fleetflow.messaging.event.EventEnvelope;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OperationalEventConsumer {
    private final ObjectMapper objectMapper;
    private final AIEventContextService context;

    public OperationalEventConsumer(ObjectMapper objectMapper, AIEventContextService context) {
        this.objectMapper = objectMapper;
        this.context = context;
    }

    @KafkaListener(topics = {
            "${fleetflow.messaging.topics.shipment:fleetflow.shipment.events.v1}",
            "${fleetflow.messaging.topics.vehicle:fleetflow.vehicle.events.v1}",
            "${fleetflow.messaging.topics.driver:fleetflow.driver.events.v1}",
            "${fleetflow.messaging.topics.incident:fleetflow.incident.events.v1}"
    }, groupId = "fleetflow-ai-operational-context", containerFactory = "aiOperationalKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) throws Exception {
        EventEnvelope<?> event = objectMapper.readValue(record.value(), EventEnvelope.class);
        context.record(event);
    }
}
