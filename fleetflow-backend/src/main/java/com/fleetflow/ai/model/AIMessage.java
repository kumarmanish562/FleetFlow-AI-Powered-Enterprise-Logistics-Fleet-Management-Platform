package com.fleetflow.ai.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_messages")
public class AIMessage {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false) private AIConversation conversation;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private MessageRole role;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected AIMessage() {}
    public AIMessage(UUID id, AIConversation conversation, MessageRole role, String content) {
        this.id = id; this.conversation = conversation; this.role = role; this.content = content;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public MessageRole getRole() { return role; }
    public String getContent() { return content; }
}
