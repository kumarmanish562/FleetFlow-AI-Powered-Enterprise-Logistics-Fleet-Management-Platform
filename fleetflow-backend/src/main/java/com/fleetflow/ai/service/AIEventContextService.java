package com.fleetflow.ai.service;

import com.fleetflow.messaging.consumer.ProcessedEventRepository;
import com.fleetflow.messaging.event.EventEnvelope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Stores metadata only; event payloads are deliberately not copied into AI context. */
@Service
public class AIEventContextService {
    private static final String CONSUMER = "fleetflow-ai-operational-context";
    private static final List<String> ALLOWED_TYPES = List.of(
            "ShipmentCreated", "ShipmentAssigned", "ShipmentPickedUp", "ShipmentDelayed", "ShipmentDelivered",
            "VehicleStatusChanged", "VehicleMaintenanceScheduled", "DriverAvailabilityChanged",
            "IncidentReported", "IncidentResolved");

    private final ProcessedEventRepository processed;
    private final JdbcTemplate jdbc;

    public AIEventContextService(ProcessedEventRepository processed, JdbcTemplate jdbc) {
        this.processed = processed;
        this.jdbc = jdbc;
    }

    @Transactional
    public void record(EventEnvelope<?> event) {
        if (event == null || event.eventId() == null || event.eventType() == null
                || event.aggregateType() == null || event.aggregateId() == null
                || !ALLOWED_TYPES.contains(event.eventType())) return;
        UUID organizationId = resolveOrganization(event.aggregateType(), event.aggregateId());
        if (organizationId == null) return;
        if (!processed.claim(CONSUMER, event.eventId())) return;
        jdbc.update("""
                INSERT INTO ai_operational_events(event_id,event_type,aggregate_type,aggregate_id,occurred_at,organization_id)
                VALUES (?,?,?,?,?,?) ON CONFLICT (event_id) DO NOTHING
                """, event.eventId(), event.eventType(), event.aggregateType(), event.aggregateId(),
                event.occurredAt() == null ? Instant.now() : event.occurredAt(), organizationId);
        // Keep only a bounded 90-day event window. The domain database remains authoritative.
        jdbc.update("DELETE FROM ai_operational_events WHERE occurred_at < CURRENT_TIMESTAMP - INTERVAL '90 days'");
    }

    @Transactional(readOnly = true)
    public List<OperationalEvent> recent(String aggregateType, String aggregateId, UUID organizationId, int limit) {
        int safeLimit = Math.max(1, Math.min(20, limit));
        return jdbc.query("""
                SELECT event_id,event_type,occurred_at FROM ai_operational_events
                WHERE aggregate_type=? AND aggregate_id=? AND organization_id=?
                ORDER BY occurred_at DESC,event_id DESC LIMIT ?
                """, (rs, row) -> new OperationalEvent(rs.getObject("event_id", UUID.class),
                rs.getString("event_type"), rs.getTimestamp("occurred_at").toInstant()),
                aggregateType, aggregateId, organizationId, safeLimit);
    }

    private UUID resolveOrganization(String aggregateType, String aggregateId) {
        try {
            UUID id = UUID.fromString(aggregateId);
            String type = aggregateType.toLowerCase(java.util.Locale.ROOT);
            String sql = switch (type) {
                case "shipment" -> "SELECT organization_id FROM shipments WHERE id=?";
                case "vehicle" -> "SELECT organization_id FROM vehicles WHERE id=?";
                case "driver" -> "SELECT u.organization_id FROM drivers d JOIN users u ON u.id=d.user_id WHERE d.id=?";
                case "incident", "operationalincident" -> "SELECT organization_id FROM operational_incidents WHERE id=?";
                case "maintenance", "maintenancerecord" -> "SELECT organization_id FROM maintenance_records WHERE id=?";
                default -> null;
            };
            if (sql == null) return null;
            return jdbc.queryForObject(sql, UUID.class, id);
        } catch (IllegalArgumentException | org.springframework.dao.EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Scheduled(cron = "0 35 * * * *")
    public void purgeExpiredIdempotencyClaims() {
        jdbc.update("DELETE FROM processed_events WHERE consumer_name=? AND processed_at < CURRENT_TIMESTAMP - INTERVAL '90 days'",
                CONSUMER);
    }

    public record OperationalEvent(UUID eventId, String eventType, Instant occurredAt) {}
}
