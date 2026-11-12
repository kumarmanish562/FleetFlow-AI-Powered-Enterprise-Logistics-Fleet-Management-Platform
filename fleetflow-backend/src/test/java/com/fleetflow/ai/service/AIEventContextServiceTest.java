package com.fleetflow.ai.service;

import com.fleetflow.messaging.consumer.ProcessedEventRepository;
import com.fleetflow.messaging.event.EventEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AIEventContextServiceTest {
    private final ProcessedEventRepository processed = mock(ProcessedEventRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AIEventContextService service = new AIEventContextService(processed, jdbc);
    private final UUID eventId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(jdbc.queryForObject(anyString(), eq(UUID.class), eq(shipmentId))).thenReturn(organizationId);
    }

    @Test
    void recordsOnlyAllowListedEventsAfterResolvingTenant() {
        when(processed.claim("fleetflow-ai-operational-context", eventId)).thenReturn(true);
        var event = new EventEnvelope<>(eventId, "ShipmentDelivered", 1, Instant.now(),
                "Shipment", shipmentId.toString(), null, "payload intentionally ignored");

        service.record(event);

        verify(processed).claim("fleetflow-ai-operational-context", eventId);
        verify(jdbc).update(contains("INSERT INTO ai_operational_events"), eq(eventId), eq("ShipmentDelivered"),
                eq("Shipment"), eq(shipmentId.toString()), any(Instant.class), eq(organizationId));
    }

    @Test
    void duplicateEventsDoNotCreateAnotherContextRow() {
        when(processed.claim("fleetflow-ai-operational-context", eventId)).thenReturn(false);
        var event = new EventEnvelope<>(eventId, "ShipmentCreated", 1, Instant.now(),
                "Shipment", shipmentId.toString(), null, null);

        service.record(event);

        verify(processed).claim("fleetflow-ai-operational-context", eventId);
        verify(jdbc, never()).update(contains("INSERT INTO ai_operational_events"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void unsupportedEventsAndMalformedAggregateIdsAreIgnored() {
        service.record(new EventEnvelope<>(eventId, "UnknownEvent", 1, Instant.now(),
                "Shipment", shipmentId.toString(), null, null));
        service.record(new EventEnvelope<>(eventId, "ShipmentCreated", 1, Instant.now(),
                "Shipment", "not-a-uuid", null, null));

        verifyNoInteractions(processed, jdbc);
    }
}
