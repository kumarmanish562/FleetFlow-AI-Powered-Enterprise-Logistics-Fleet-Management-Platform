package com.fleetflow.ai.controller;

import com.fleetflow.ai.dto.request.CustomerAccessGrantRequest;
import com.fleetflow.ai.service.AICustomerAccessService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@ConditionalOnProperty(prefix = "fleetflow.ai", name = "enabled", havingValue = "true")
@RequestMapping("/api/v1/ai/operational/customer-access")
public class AICustomerAccessController {
    private final AICustomerAccessService service;

    public AICustomerAccessController(AICustomerAccessService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> grant(@Valid @RequestBody CustomerAccessGrantRequest request,
                                      Authentication authentication) {
        service.grant(request.userId(), request.customerId(), authentication);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> revoke(@PathVariable java.util.UUID userId, Authentication authentication) {
        service.revoke(userId, authentication);
        return ResponseEntity.noContent().build();
    }
}
