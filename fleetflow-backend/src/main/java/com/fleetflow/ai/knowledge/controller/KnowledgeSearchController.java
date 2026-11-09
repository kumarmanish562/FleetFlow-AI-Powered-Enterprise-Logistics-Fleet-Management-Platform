package com.fleetflow.ai.knowledge.controller;

import com.fleetflow.ai.knowledge.dto.KnowledgeSearchRequest;
import com.fleetflow.ai.knowledge.dto.KnowledgeSource;
import com.fleetflow.ai.knowledge.service.KnowledgeRetrievalService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/knowledge")
@ConditionalOnProperty(prefix = "fleetflow.knowledge", name = "enabled", havingValue = "true")
public class KnowledgeSearchController {
    private final KnowledgeRetrievalService retrieval;
    public KnowledgeSearchController(KnowledgeRetrievalService retrieval) { this.retrieval = retrieval; }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','DRIVER','CUSTOMER','FINANCE_MANAGER')")
    public List<KnowledgeSource> search(@Valid @RequestBody KnowledgeSearchRequest request, Authentication authentication) {
        return retrieval.retrieve(request.query(), authentication);
    }
}
