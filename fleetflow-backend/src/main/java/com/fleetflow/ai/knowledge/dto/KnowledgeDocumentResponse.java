package com.fleetflow.ai.knowledge.dto;

import com.fleetflow.ai.knowledge.model.DocumentStatus;
import com.fleetflow.ai.knowledge.model.DocumentType;
import com.fleetflow.ai.knowledge.model.KnowledgeDocument;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record KnowledgeDocumentResponse(UUID id, String filename, DocumentType documentType, String version,
                                        DocumentStatus status, List<String> accessRoles, int chunkCount,
                                        UUID replacesDocumentId, Instant createdAt, Instant indexedAt,
                                        String errorMessage) {
    public static KnowledgeDocumentResponse from(KnowledgeDocument d) {
        return new KnowledgeDocumentResponse(d.getId(), d.getFilename(), d.getDocumentType(), d.getVersion(),
                d.getStatus(), d.getAccessRoles(), d.getChunkCount(), d.getReplacesDocumentId(),
                d.getCreatedAt(), d.getIndexedAt(), d.getErrorMessage());
    }
}
