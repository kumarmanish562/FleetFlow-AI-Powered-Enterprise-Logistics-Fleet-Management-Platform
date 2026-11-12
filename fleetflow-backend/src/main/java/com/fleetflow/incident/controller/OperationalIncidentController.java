package com.fleetflow.incident.controller;

import com.fleetflow.incident.dto.IncidentCreateRequest;
import com.fleetflow.incident.dto.IncidentResponse;
import com.fleetflow.incident.service.OperationalIncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
public class OperationalIncidentController {
    private final OperationalIncidentService incidents;

    public OperationalIncidentController(OperationalIncidentService incidents) { this.incidents = incidents; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public IncidentResponse report(@Valid @RequestBody IncidentCreateRequest request, Authentication authentication) {
        return incidents.report(request, authentication);
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public IncidentResponse resolve(@PathVariable UUID id, Authentication authentication) {
        return incidents.resolve(id, authentication);
    }
}
