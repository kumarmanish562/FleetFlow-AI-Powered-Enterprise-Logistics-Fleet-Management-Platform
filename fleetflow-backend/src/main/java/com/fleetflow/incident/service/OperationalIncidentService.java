package com.fleetflow.incident.service;

import com.fleetflow.common.exception.BadRequestException;
import com.fleetflow.incident.dto.IncidentCreateRequest;
import com.fleetflow.incident.dto.IncidentResponse;
import com.fleetflow.incident.entity.OperationalIncident;
import com.fleetflow.incident.repository.OperationalIncidentRepository;
import com.fleetflow.incident.event.IncidentReportedEvent;
import com.fleetflow.incident.event.IncidentResolvedEvent;
import com.fleetflow.messaging.outbox.OutboxService;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.user.repository.UserRepository;
import com.fleetflow.user.service.CurrentOrganizationProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class OperationalIncidentService {
    private final OperationalIncidentRepository incidents;
    private final ShipmentRepository shipments;
    private final UserRepository users;
    private final CurrentOrganizationProvider organizations;
    private final OutboxService outbox;

    public OperationalIncidentService(OperationalIncidentRepository incidents, ShipmentRepository shipments,
            UserRepository users, CurrentOrganizationProvider organizations, OutboxService outbox) {
        this.incidents = incidents;
        this.shipments = shipments;
        this.users = users;
        this.organizations = organizations;
        this.outbox = outbox;
    }

    @Transactional
    public IncidentResponse report(IncidentCreateRequest request, Authentication authentication) {
        UUID organizationId = organizations.requireOrganizationId();
        var shipment = shipments.findByIdAndOrganizationId(request.shipmentId(), organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
        var user = users.findByEmailIgnoreCase(authentication.getName())
                .filter(candidate -> organizationId.equals(candidate.getOrganizationId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String description = request.description() == null ? null : request.description().trim();
        OperationalIncident incident = OperationalIncident.report(UUID.randomUUID(), organizationId, shipment,
                request.category(), request.severity(), request.summary().trim(), description,
                user.getId(), OffsetDateTime.now(ZoneOffset.UTC));
        OperationalIncident saved = incidents.save(incident);
        outbox.record(outbox.incidentTopic(), "IncidentReported", "OperationalIncident",
                saved.getId().toString(), null, new IncidentReportedEvent(saved.getId(), shipment.getId(),
                        saved.getReportedAt().toInstant()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> listForOperationalAI(UUID organizationId, UUID customerId,
            LocalDate from, LocalDate to, Pageable pageable) {
        validateRange(from, to);
        if (pageable.getPageNumber() != 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 100)
            throw new BadRequestException("Incident query must be limited to the first 100 results");
        OffsetDateTime start = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return incidents.findForOperationalAI(organizationId, customerId, start, end, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public IncidentResponse resolve(UUID incidentId, Authentication authentication) {
        UUID organizationId = organizations.requireOrganizationId();
        var incident = incidents.findByIdAndOrganizationId(incidentId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
        var user = users.findByEmailIgnoreCase(authentication.getName())
                .filter(candidate -> organizationId.equals(candidate.getOrganizationId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        boolean wasOpen = incident.getStatus() == com.fleetflow.incident.enums.IncidentStatus.OPEN;
        incident.resolve(user.getId(), OffsetDateTime.now(ZoneOffset.UTC));
        if (wasOpen) outbox.record(outbox.incidentTopic(), "IncidentResolved", "OperationalIncident",
                incident.getId().toString(), null, new IncidentResolvedEvent(incident.getId(),
                        incident.getShipment().getId(), incident.getResolvedAt().toInstant()));
        return toResponse(incident);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > 30)
            throw new BadRequestException("Incident date range must be ordered and no longer than 31 days");
    }

    private IncidentResponse toResponse(OperationalIncident incident) {
        var shipment = incident.getShipment();
        return new IncidentResponse(incident.getId(), shipment.getId(), shipment.getShipmentNumber(),
                incident.getCategory(), incident.getSeverity(), incident.getStatus(), incident.getSummary(),
                incident.getDescription(), incident.getReportedAt(), incident.getResolvedAt());
    }
}
