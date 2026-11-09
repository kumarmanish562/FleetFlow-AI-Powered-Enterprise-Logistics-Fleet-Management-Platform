package com.fleetflow.ai.controller;

import com.fleetflow.ai.dto.request.AIChatRequest;
import com.fleetflow.ai.dto.response.AIChatResponse;
import com.fleetflow.ai.service.AIChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@ConditionalOnProperty(prefix = "fleetflow.ai", name = "enabled", havingValue = "true")
@RequestMapping("/api/v1/ai")
public class AIChatController {
    private final AIChatService service;
    public AIChatController(AIChatService service) { this.service = service; }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER', 'CUSTOMER', 'FINANCE_MANAGER')")
    public ResponseEntity<AIChatResponse> chat(@Valid @RequestBody AIChatRequest request,
                                               Authentication authentication) {
        return ResponseEntity.ok(service.chat(request, authentication.getName()));
    }
}
