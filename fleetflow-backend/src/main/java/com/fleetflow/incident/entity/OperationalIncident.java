package com.fleetflow.incident.entity;

import com.fleetflow.incident.enums.IncidentCategory;
import com.fleetflow.incident.enums.IncidentSeverity;
import com.fleetflow.incident.enums.IncidentStatus;
import com.fleetflow.shipment.entity.Shipment;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "operational_incidents", indexes = {
        @Index(name = "idx_operational_incidents_org_time", columnList = "organization_id,reported_at"),
        @Index(name = "idx_operational_incidents_org_status", columnList = "organization_id,status,severity")
})
public class OperationalIncident {
    @Id private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private IncidentCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private IncidentSeverity severity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private IncidentStatus status;
    @Column(nullable = false, length = 200) private String summary;
    @Column(length = 1000) private String description;
    @Column(name = "reported_by", nullable = false) private UUID reportedBy;
    @Column(name = "reported_at", nullable = false) private OffsetDateTime reportedAt;
    @Column(name = "resolved_by") private UUID resolvedBy;
    @Column(name = "resolved_at") private OffsetDateTime resolvedAt;

    protected OperationalIncident() {}

    public static OperationalIncident report(UUID id, UUID organizationId, Shipment shipment,
            IncidentCategory category, IncidentSeverity severity, String summary, String description,
            UUID reporter, OffsetDateTime reportedAt) {
        var incident = new OperationalIncident();
        incident.id = id; incident.organizationId = organizationId; incident.shipment = shipment;
        incident.category = category; incident.severity = severity; incident.status = IncidentStatus.OPEN;
        incident.summary = summary; incident.description = description; incident.reportedBy = reporter;
        incident.reportedAt = reportedAt;
        return incident;
    }

    public void resolve(UUID userId, OffsetDateTime at) {
        if (status == IncidentStatus.RESOLVED) return;
        status = IncidentStatus.RESOLVED; resolvedBy = userId; resolvedAt = at;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public Shipment getShipment() { return shipment; }
    public IncidentCategory getCategory() { return category; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public OffsetDateTime getReportedAt() { return reportedAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
}
