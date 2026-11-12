package com.fleetflow.ai.dto.response;

import java.util.UUID;
import java.util.List;
import com.fleetflow.ai.knowledge.dto.KnowledgeSource;
import com.fleetflow.ai.dto.response.OperationalFact;

public record AIChatResponse(UUID conversationId, UUID messageId, String answer, String status,
                             List<KnowledgeSource> sources, List<String> toolReferences,
                             List<OperationalFact> verifiedFacts,
                             List<String> possibleExplanations, List<String> recommendations,
                             boolean hasSufficientEvidence) {
    public AIChatResponse {
        sources = sources == null ? List.of() : List.copyOf(sources);
        toolReferences = toolReferences == null ? List.of() : List.copyOf(toolReferences);
        verifiedFacts = verifiedFacts == null ? List.of() : List.copyOf(verifiedFacts);
        possibleExplanations = possibleExplanations == null ? List.of() : List.copyOf(possibleExplanations);
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }
    public AIChatResponse(UUID conversationId, UUID messageId, String answer, String status, List<KnowledgeSource> sources) {
        this(conversationId, messageId, answer, status, sources, List.of(), List.of(), List.of(), List.of(), false);
    }
    public AIChatResponse(UUID conversationId, UUID messageId, String answer, String status,
                          List<KnowledgeSource> sources, List<String> toolReferences) {
        this(conversationId, messageId, answer, status, sources, toolReferences, List.of(), List.of(), List.of(), false);
    }
    public AIChatResponse(UUID conversationId, String answer, String status, List<KnowledgeSource> sources) {
        this(conversationId, null, answer, status, sources, List.of(), List.of(), List.of(), List.of(), false);
    }
    public AIChatResponse(UUID conversationId, String answer, String status) {
        this(conversationId, null, answer, status, List.of(), List.of(), List.of(), List.of(), List.of(), false);
    }
}
