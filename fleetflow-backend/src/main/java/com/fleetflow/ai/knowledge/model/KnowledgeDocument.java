package com.fleetflow.ai.knowledge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "knowledge_documents")
@Getter @Setter @NoArgsConstructor
public class KnowledgeDocument {
    @Id private UUID id;
    @Column(name = "organization_id", nullable = false) private UUID organizationId;
    @Column(name = "uploaded_by", nullable = false) private UUID uploadedBy;
    @Column(nullable = false, length = 255) private String filename;
    @Column(name = "storage_key", nullable = false, length = 500) private String storageKey;
    @Enumerated(EnumType.STRING) @Column(name = "document_type", nullable = false, length = 60)
    private DocumentType documentType;
    @Column(nullable = false, length = 50) private String version;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private DocumentStatus status;
    @Column(nullable = false, length = 64) private String checksum;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "access_roles", nullable = false, columnDefinition = "jsonb")
    private List<String> accessRoles;
    @Column(name = "chunk_count", nullable = false) private int chunkCount;
    @Column(name = "active_generation") private UUID activeGeneration;
    @Column(name = "replaces_document_id") private UUID replacesDocumentId;
    @Column(name = "error_message") private String errorMessage;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "indexed_at") private Instant indexedAt;
    @Column(name = "deleted_at") private Instant deletedAt;

    @PrePersist void beforeInsert() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
}
