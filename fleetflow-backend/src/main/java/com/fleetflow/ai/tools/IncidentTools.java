package com.fleetflow.ai.tools;

import com.fleetflow.ai.security.AIDataAccessPolicy;
import com.fleetflow.incident.service.OperationalIncidentService;
import com.fleetflow.incident.dto.IncidentResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class IncidentTools {
    private final OperationalIncidentService incidents;
    private final AIDataAccessPolicy access;

    public IncidentTools(OperationalIncidentService incidents, AIDataAccessPolicy access) {
        this.incidents = incidents;
        this.access = access;
    }

    @Tool(description = "Summarize reported delivery and operational incidents in the authenticated organization during an inclusive date range (maximum 31 days). Results are capped at 50; summaries exclude reporter identity and contact details.")
    public IncidentListFact getDeliveryIncidents(
            @ToolParam(description = "Start date, YYYY-MM-DD") LocalDate from,
            @ToolParam(description = "End date, YYYY-MM-DD") LocalDate to) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getDeliveryIncidents", scope -> {
            var page = incidents.listForOperationalAI(scope.organizationId(), scope.customerId(), from, to,
                    PageRequest.of(0, 50, org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Direction.DESC, "reportedAt")));
            List<IncidentFact> facts = page.getContent().stream().map(IncidentTools::toFact).toList();
            return new IncidentListFact(facts, page.getTotalElements() > page.getNumberOfElements());
        });
    }

    private static IncidentFact toFact(IncidentResponse incident) {
        String summary = incident.summary().replaceAll("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", "[redacted email]")
                .replaceAll("\\+?[0-9][0-9() .-]{7,}[0-9]", "[redacted phone]");
        return new IncidentFact(incident.id(), incident.shipmentNumber(), incident.category().name(),
                incident.severity().name(), incident.status().name(), summary, incident.reportedAt());
    }

    public record IncidentFact(UUID incidentId, String shipmentNumber, String category, String severity,
                               String status, String summary, java.time.OffsetDateTime reportedAt) {}
    public record IncidentListFact(List<IncidentFact> incidents, boolean mayBeTruncated) {}
}
