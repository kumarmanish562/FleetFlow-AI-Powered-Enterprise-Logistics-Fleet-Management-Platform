package com.fleetflow.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.messaging.config.MessagingProperties;
import com.fleetflow.messaging.event.EventEnvelope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final MessagingProperties properties;

    public OutboxService(
            OutboxRepository repository,
            ObjectMapper objectMapper,
            MessagingProperties properties
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional
    public void record(
            String topic,
            String eventType,
            String aggregateType,
            String aggregateId,
            String correlationId,
            Object payload
    ) {
        UUID eventId = UUID.randomUUID();

        EventEnvelope<Object> envelope = new EventEnvelope<>(
                eventId,
                eventType,
                1,
                Instant.now(),
                aggregateType,
                aggregateId,
                correlationId,
                payload
        );

        try {
            String json = objectMapper.writeValueAsString(envelope);

            repository.save(OutboxEvent.create(
                    eventId,
                    aggregateType,
                    aggregateId,
                    eventType,
                    1,
                    topic,
                    json,
                    correlationId
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize event " + eventType,
                    exception
            );
        }
    }

    public String shipmentTopic() {
        return properties.topics().shipment();
    }

    public String vehicleTopic() {
        return properties.topics().vehicle();
    }

    public String driverTopic() {
        return properties.topics().driver();
    }

    public String incidentTopic() { return properties.topics().incident(); }
}
