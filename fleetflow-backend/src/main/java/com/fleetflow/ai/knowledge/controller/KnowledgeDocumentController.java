package com.fleetflow.ai.knowledge.controller;

import com.fleetflow.ai.knowledge.dto.*;
import com.fleetflow.ai.knowledge.model.DocumentType;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import com.fleetflow.ai.knowledge.service.KnowledgeDocumentService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/knowledge/documents")
@ConditionalOnProperty(prefix = "fleetflow.knowledge", name = "enabled", havingValue = "true")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class KnowledgeDocumentController {
    private final KnowledgeDocumentService service;
    private final KnowledgePrincipalService principals;

    public KnowledgeDocumentController(KnowledgeDocumentService service, KnowledgePrincipalService principals) {
        this.service = service; this.principals = principals;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<KnowledgeDocumentResponse> upload(@RequestPart("file") MultipartFile file,
            @RequestParam DocumentType documentType, @RequestParam String version,
            @RequestParam List<String> accessRoles,
            @RequestParam(required = false) UUID replacesDocumentId, Authentication authentication) {
        var result = service.upload(file, documentType, version, accessRoles, replacesDocumentId,
                principals.resolveAdmin(authentication));
        return ResponseEntity.accepted().location(URI.create("/api/v1/ai/knowledge/documents/" + result.id())).body(result);
    }

    @GetMapping
    public Page<KnowledgeDocumentResponse> list(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size, Authentication authentication) {
        return service.list(principals.resolveAdmin(authentication),
                PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/{id}")
    public KnowledgeDocumentResponse get(@PathVariable UUID id, Authentication authentication) {
        return service.get(id, principals.resolveAdmin(authentication));
    }

    @GetMapping("/{id}/status")
    public KnowledgeStatusResponse status(@PathVariable UUID id, Authentication authentication) {
        return service.status(id, principals.resolveAdmin(authentication));
    }

    @PostMapping("/{id}/reindex")
    public ResponseEntity<KnowledgeStatusResponse> reindex(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.accepted().body(service.reindex(id, principals.resolveAdmin(authentication)));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<KnowledgeStatusResponse> retry(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.accepted().body(service.retry(id, principals.resolveAdmin(authentication)));
    }

    @PutMapping("/{id}/access")
    public KnowledgeDocumentResponse updateAccess(@PathVariable UUID id, @Valid @RequestBody AccessUpdateRequest body,
                                                  Authentication authentication) {
        return service.updateAccess(id, body, principals.resolveAdmin(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<KnowledgeStatusResponse> delete(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.accepted().body(service.delete(id, principals.resolveAdmin(authentication)));
    }
}
