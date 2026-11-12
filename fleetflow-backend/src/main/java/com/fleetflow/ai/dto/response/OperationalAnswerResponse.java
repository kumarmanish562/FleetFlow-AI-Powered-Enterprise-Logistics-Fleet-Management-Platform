package com.fleetflow.ai.dto.response;

import com.fleetflow.ai.dto.response.AIChatResponse;
import com.fleetflow.ai.knowledge.dto.KnowledgeSource;

import java.util.List;
import java.util.UUID;

public record OperationalAnswerResponse(UUID conversationId, UUID messageId, String status, String answer,
        List<OperationalFact> verifiedFacts, List<String> possibleExplanations,
        List<String> recommendations, List<KnowledgeSource> sources,
        List<String> toolReferences, boolean hasSufficientEvidence) {
    public OperationalAnswerResponse {
        verifiedFacts = List.copyOf(verifiedFacts);
        possibleExplanations = List.copyOf(possibleExplanations);
        recommendations = List.copyOf(recommendations);
        sources = List.copyOf(sources);
        toolReferences = List.copyOf(toolReferences);
    }

    public static OperationalAnswerResponse from(AIChatResponse response) {
        return new OperationalAnswerResponse(response.conversationId(), response.messageId(), response.status(),
                response.answer(), response.verifiedFacts(),
                response.possibleExplanations(), response.recommendations(), response.sources(),
                response.toolReferences(), response.hasSufficientEvidence());
    }
}
