package com.fleetflow.ai.service;

import com.fleetflow.ai.knowledge.repository.KnowledgeDocumentRepository;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import com.fleetflow.ai.knowledge.storage.KnowledgeFileStorage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.common.exception.ResourceNotFoundException;
import com.fleetflow.common.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Path;
import java.util.*;

@Service
public class AIConversationManagementService {
    private final JdbcTemplate jdbc;
    private final KnowledgePrincipalService principals;
    private final KnowledgeDocumentRepository documents;
    private final KnowledgeFileStorage files;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public AIConversationManagementService(JdbcTemplate jdbc, KnowledgePrincipalService principals,
            KnowledgeDocumentRepository documents, KnowledgeFileStorage files, ObjectMapper objectMapper) {
        this.jdbc = jdbc; this.principals = principals; this.documents = documents; this.files = files;
        this.objectMapper = objectMapper;
    }

    public AIConversationManagementService(JdbcTemplate jdbc, KnowledgePrincipalService principals,
            KnowledgeDocumentRepository documents, KnowledgeFileStorage files) {
        this(jdbc, principals, documents, files, new ObjectMapper());
    }

    public List<Map<String,Object>> list(Authentication auth) {
        var p = principals.resolve(auth);
        return jdbc.queryForList("SELECT id,title,created_at,updated_at FROM ai_conversations WHERE user_id=? AND organization_id=? AND deleted_at IS NULL AND expires_at>CURRENT_TIMESTAMP ORDER BY updated_at DESC LIMIT 100", p.userId(), p.organizationId());
    }

    public Map<String,Object> get(UUID id, Authentication auth) {
        var p = principals.resolve(auth);
        var rows = jdbc.queryForList("SELECT id,title,created_at,updated_at FROM ai_conversations WHERE id=? AND user_id=? AND organization_id=? AND deleted_at IS NULL AND expires_at>CURRENT_TIMESTAMP", id, p.userId(), p.organizationId());
        if (rows.isEmpty()) throw new ResourceNotFoundException("AI conversation not found");
        var result = new LinkedHashMap<String,Object>(rows.get(0));
        result.put("messages", jdbc.queryForList("SELECT id,role,content,created_at FROM ai_messages WHERE conversation_id=? ORDER BY created_at,id", id));
        return result;
    }

    public void delete(UUID id, Authentication auth) {
        var p = principals.resolve(auth);
        int updated = jdbc.update("UPDATE ai_conversations SET deleted_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=? AND organization_id=? AND deleted_at IS NULL", id, p.userId(), p.organizationId());
        if (updated == 0) throw new ResourceNotFoundException("AI conversation not found");
    }

    @Transactional
    public void feedback(UUID messageId, String type, String comment, Authentication auth) {
        if (type == null || !Set.of("POSITIVE", "NEGATIVE").contains(type)) throw new BadRequestException("Feedback type must be POSITIVE or NEGATIVE");
        if (comment != null && comment.length() > 1000) throw new BadRequestException("Feedback comment must be at most 1000 characters");
        var p = principals.resolve(auth);
        Integer owned = jdbc.queryForObject("SELECT COUNT(*) FROM ai_messages m JOIN ai_conversations c ON c.id=m.conversation_id WHERE m.id=? AND m.role='ASSISTANT' AND c.user_id=? AND c.organization_id=? AND c.deleted_at IS NULL", Integer.class, messageId, p.userId(), p.organizationId());
        if (owned == null || owned == 0) throw new ResourceNotFoundException("AI message not found");
        jdbc.update("INSERT INTO ai_message_feedback(id,message_id,user_id,feedback_type,comment) VALUES (?,?,?,?,?) ON CONFLICT (message_id,user_id) DO UPDATE SET feedback_type=EXCLUDED.feedback_type,comment=EXCLUDED.comment,created_at=CURRENT_TIMESTAMP", UUID.randomUUID(), messageId, p.userId(), type, comment);
    }

    public Map<String,Object> sources(UUID messageId, Authentication auth) {
        var p = principals.resolve(auth);
        Integer owned = jdbc.queryForObject("SELECT COUNT(*) FROM ai_messages m JOIN ai_conversations c ON c.id=m.conversation_id WHERE m.id=? AND c.user_id=? AND c.organization_id=? AND c.deleted_at IS NULL", Integer.class, messageId, p.userId(), p.organizationId());
        if (owned == null || owned == 0) throw new ResourceNotFoundException("AI message not found");
        var stored = jdbc.queryForList("SELECT document_id AS \"documentId\",chunk_id AS \"chunkId\",filename,version,page_number AS \"pageNumber\",excerpt,similarity FROM ai_message_sources WHERE message_id=? ORDER BY ordinal", messageId);
        List<Map<String,Object>> authorized = new ArrayList<>();
        for (var source : stored) {
            UUID documentId = (UUID) source.get("documentId");
            documents.findByIdAndOrganizationId(documentId, p.organizationId())
                    .filter(doc -> doc.getStatus().name().equals("INDEXED") && doc.getAccessRoles() != null && doc.getAccessRoles().stream().anyMatch(p.roles()::contains))
                    .ifPresent(doc -> authorized.add(source));
        }
        List<Map<String,Object>> evidenceRows = jdbc.queryForList("""
                SELECT tool_name AS "toolName", source_type AS "source",
                       COALESCE(evidence->>'value', evidence::text) AS "value",
                       COALESCE(evidence->'referenceIds', '[]'::jsonb)::text AS "referenceIds",
                       observed_at AS "observedAt"
                FROM ai_message_operational_evidence WHERE message_id=? ORDER BY ordinal
                """, messageId);
        List<Map<String,Object>> operationalEvidence = evidenceRows.stream().map(row -> {
            Map<String,Object> item = new LinkedHashMap<>(row);
            item.put("referenceIds", parseStringList((String) item.get("referenceIds")));
            return item;
        }).toList();
        List<String> toolReferences = operationalEvidence.stream()
                .map(row -> (String) row.get("toolName")).distinct().toList();
        Map<String,Object> sections = jdbc.query("""
                SELECT possible_explanations::text AS explanations, recommendations::text AS recommendations,
                       has_sufficient_evidence AS sufficient
                FROM ai_message_response_sections WHERE message_id=?
                """, rs -> rs.next() ? Map.of("explanations", rs.getString("explanations"),
                        "recommendations", rs.getString("recommendations"), "sufficient", rs.getBoolean("sufficient")) : Map.of(),
                messageId);
        if (sections == null) sections = Map.of();
        List<String> explanations = parseStringList((String) sections.get("explanations"));
        List<String> recommendations = parseStringList((String) sections.get("recommendations"));
        boolean sufficient = Boolean.TRUE.equals(sections.get("sufficient"));
        return Map.of("messageId", messageId, "sources", authorized,
                "verifiedFacts", operationalEvidence, "toolReferences", toolReferences,
                "possibleExplanations", explanations, "recommendations", recommendations,
                "hasSufficientEvidence", sufficient);
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); }
        catch (Exception ex) { return List.of(); }
    }

    public Path openSource(UUID documentId, Authentication auth) {
        var p = principals.resolve(auth);
        var d = documents.findByIdAndOrganizationId(documentId, p.organizationId())
                .filter(doc -> doc.getStatus().name().equals("INDEXED") && doc.getAccessRoles() != null && doc.getAccessRoles().stream().anyMatch(p.roles()::contains))
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge document not found"));
        Path path = files.resolve(d.getStorageKey());
        if (!java.nio.file.Files.isRegularFile(path)) throw new ResourceNotFoundException("Knowledge document not found");
        return path;
    }

    @Scheduled(cron = "0 17 * * * *")
    public void purgeExpiredConversations() { jdbc.queryForObject("SELECT purge_expired_ai_conversations()", Integer.class); }
}
