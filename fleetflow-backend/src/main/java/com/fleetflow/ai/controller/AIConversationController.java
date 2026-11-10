package com.fleetflow.ai.controller;

import com.fleetflow.ai.dto.request.AIFeedbackRequest;
import com.fleetflow.ai.service.AIConversationManagementService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/ai")
@ConditionalOnProperty(prefix="fleetflow.ai", name="enabled", havingValue="true")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER','DRIVER','CUSTOMER','FINANCE_MANAGER')")
public class AIConversationController {
    private final AIConversationManagementService service;
    public AIConversationController(AIConversationManagementService service) { this.service = service; }

    @GetMapping("/conversations") public List<Map<String,Object>> list(Authentication auth) { return service.list(auth); }
    @GetMapping("/conversations/{id}") public Map<String,Object> get(@PathVariable UUID id, Authentication auth) { return service.get(id, auth); }
    @DeleteMapping("/conversations/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) { service.delete(id, auth); return ResponseEntity.noContent().build(); }
    @PostMapping("/messages/{id}/feedback") public ResponseEntity<Void> feedback(@PathVariable UUID id, @Valid @RequestBody AIFeedbackRequest body, Authentication auth) { service.feedback(id, body.type(), body.comment(), auth); return ResponseEntity.noContent().build(); }
    @GetMapping("/messages/{id}/sources") public Map<String,Object> sources(@PathVariable UUID id, Authentication auth) { return service.sources(id, auth); }
    @GetMapping("/sources/{documentId}") public ResponseEntity<FileSystemResource> source(@PathVariable UUID documentId, Authentication auth) {
        var path = service.openSource(documentId, auth);
        MediaType type = path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString().replace("\"", "") + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .contentType(type).body(new FileSystemResource(path));
    }
}
