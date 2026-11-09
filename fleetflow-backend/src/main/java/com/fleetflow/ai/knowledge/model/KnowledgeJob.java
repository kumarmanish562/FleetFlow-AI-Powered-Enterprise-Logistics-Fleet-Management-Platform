package com.fleetflow.ai.knowledge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_jobs")
@Getter @Setter @NoArgsConstructor
public class KnowledgeJob {
    public enum Kind { INDEX, DELETE }
    public enum State { QUEUED, RUNNING, FAILED, DONE }

    @Id private UUID id;
    @Column(name = "document_id", nullable = false, unique = true) private UUID documentId;
    @Column(name = "organization_id", nullable = false) private UUID organizationId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Kind kind;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private State state;
    @Column(name = "generation_id") private UUID generationId;
    @Column(nullable = false) private int attempts;
    @Column(name = "run_after", nullable = false) private Instant runAfter;
    @Column(name = "lease_until") private Instant leaseUntil;
    @Column(name = "error_message") private String errorMessage;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void beforeInsert() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
}
