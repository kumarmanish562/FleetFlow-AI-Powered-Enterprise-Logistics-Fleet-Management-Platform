package com.fleetflow.ai.dto.response;

import java.util.UUID;
import java.util.List;
import com.fleetflow.ai.knowledge.dto.KnowledgeSource;

public record AIChatResponse(UUID conversationId, String answer, String status, List<KnowledgeSource> sources) {
    public AIChatResponse(UUID conversationId, String answer, String status) {
        this(conversationId, answer, status, List.of());
    }
}
