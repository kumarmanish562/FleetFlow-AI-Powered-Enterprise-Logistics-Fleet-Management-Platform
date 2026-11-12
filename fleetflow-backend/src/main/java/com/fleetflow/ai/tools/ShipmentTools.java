package com.fleetflow.ai.tools;

import com.fleetflow.ai.security.AIDataAccessPolicy;
import com.fleetflow.ai.service.AIEventContextService;
import com.fleetflow.shipment.service.ShipmentService;
import com.fleetflow.tracking.service.TrackingEventService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ShipmentTools {
    private final ShipmentService shipments;
    private final TrackingEventService tracking;
    private final AIDataAccessPolicy access;
    private final AIEventContextService eventContext;

    public ShipmentTools(ShipmentService shipments, TrackingEventService tracking, AIDataAccessPolicy access,
                         AIEventContextService eventContext) {
        this.shipments = shipments;
        this.tracking = tracking;
        this.access = access;
        this.eventContext = eventContext;
    }

    @Tool(description = "Read authorized operational details for a shipment by its shipment number. Customer addresses, declared value and customer identifiers are excluded.")
    public ShipmentFact getShipmentDetails(@ToolParam(description = "Shipment number") String shipmentNumber) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getShipmentDetails", scope -> {
            String key = validateShipmentNumber(shipmentNumber);
            var s = shipments.getOperationalByShipmentNumber(key, scope.organizationId(), scope.customerId());
            return new ShipmentFact(s.id(), s.shipmentNumber(), s.status().name(), s.priority().name(),
                    s.driverId(), s.vehicleId(), s.routeId(), s.expectedDeliveryDate(), s.updatedAt(),
                    eventContext.recent("Shipment", s.id().toString(), scope.organizationId(), 10));
        });
    }

    @Tool(description = "Read the latest shipment tracking event only. Exact coordinates and unrestricted location history are not returned.")
    public TrackingFact getShipmentTracking(@ToolParam(description = "Shipment number") String shipmentNumber) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getShipmentTracking", scope -> {
            String key = validateShipmentNumber(shipmentNumber);
            var s = shipments.getOperationalByShipmentNumber(key, scope.organizationId(), scope.customerId());
            var event = tracking.getLatest(s.id());
            return new TrackingFact(event.eventType().name(), event.eventTime());
        });
    }

    private String validateShipmentNumber(String value) {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9_-]{1,49}"))
            throw new IllegalArgumentException("Invalid shipment number");
        return value;
    }

    public record ShipmentFact(UUID id, String shipmentNumber, String status, String priority, UUID driverId,
                               UUID vehicleId, UUID routeId, OffsetDateTime expectedDeliveryAt, OffsetDateTime updatedAt,
                               List<AIEventContextService.OperationalEvent> recentEvents) {}
    public record TrackingFact(String eventType, OffsetDateTime eventTime) {}
}
