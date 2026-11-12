package com.fleetflow.ai.controller;

import com.fleetflow.ai.dto.request.AIChatRequest;
import com.fleetflow.ai.dto.request.OperationalQuestionRequest;
import com.fleetflow.ai.dto.response.OperationalAnswerResponse;
import com.fleetflow.ai.dto.response.AIChatResponse;
import com.fleetflow.ai.service.AIChatService;
import com.fleetflow.ai.security.AIDataAccessPolicy;
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
    private final AIDataAccessPolicy operationalAccess;
    public AIChatController(AIChatService service, AIDataAccessPolicy operationalAccess) {
        this.service = service; this.operationalAccess = operationalAccess;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER', 'CUSTOMER', 'FINANCE_MANAGER')")
    public ResponseEntity<AIChatResponse> chat(@Valid @RequestBody AIChatRequest request,
                                               Authentication authentication) {
        return ResponseEntity.ok(service.chat(request, authentication));
    }

    @PostMapping("/operational/query")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER', 'CUSTOMER', 'FINANCE_MANAGER')")
    public ResponseEntity<OperationalAnswerResponse> operationalQuery(@Valid @RequestBody OperationalQuestionRequest request,
                                                                       Authentication authentication) {
        return ResponseEntity.ok(OperationalAnswerResponse.from(service.chat(
                new AIChatRequest(request.question(), request.conversationId()), authentication)));
    }

    @GetMapping("/operational/tools")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DISPATCHER', 'DRIVER', 'CUSTOMER', 'FINANCE_MANAGER')")
    public ResponseEntity<java.util.List<String>> availableOperationalTools(Authentication authentication) {
        return ResponseEntity.ok(operationalAccess.availableToolNames(authentication));
    }
}
