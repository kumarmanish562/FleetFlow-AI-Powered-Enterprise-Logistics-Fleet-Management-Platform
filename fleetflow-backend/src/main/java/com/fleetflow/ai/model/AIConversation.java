package com.fleetflow.ai.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_conversations")
public class AIConversation {
    @Id private UUID id;
    @Column(nullable = false, length = 254) private String username;
    @Column(name = "organization_id", nullable = false) private UUID organizationId;
    @Column(length = 200) private String title;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected AIConversation() {}
    public AIConversation(UUID id, String username, String title) {
        this(id, username, com.fleetflow.user.entity.User.DEFAULT_ORGANIZATION_ID, title);
    }
    public AIConversation(UUID id, String username, UUID organizationId, String title) {
        this.id = id; this.username = username; this.organizationId = organizationId; this.title = title;
    }
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public void touch() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public UUID getOrganizationId() { return organizationId; }
    public String getTitle() { return title; }
}
