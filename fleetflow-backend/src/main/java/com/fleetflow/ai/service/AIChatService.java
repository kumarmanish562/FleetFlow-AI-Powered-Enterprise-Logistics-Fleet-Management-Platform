package com.fleetflow.ai.service;

import com.fleetflow.ai.dto.request.AIChatRequest;
import com.fleetflow.ai.dto.response.AIChatResponse;
import com.fleetflow.ai.exception.AIConversationNotFoundException;
import com.fleetflow.ai.exception.AIProviderException;
import com.fleetflow.ai.model.AIConversation;
import com.fleetflow.ai.model.AIMessage;
import com.fleetflow.ai.model.MessageRole;
import com.fleetflow.ai.prompt.FleetFlowPromptService;
import com.fleetflow.ai.repository.AIConversationRepository;
import com.fleetflow.ai.repository.AIMessageRepository;
import com.fleetflow.ai.security.AIUsageRateLimiter;
import com.fleetflow.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@ConditionalOnProperty(prefix = "fleetflow.ai", name = "enabled", havingValue = "true")
public class AIChatService {
    private static final Logger log = LoggerFactory.getLogger(AIChatService.class);
    private final ChatClient chatClient;
    private final AIConversationRepository conversations;
    private final AIMessageRepository messages;
    private final AIUsageRateLimiter rateLimiter;
    private final FleetFlowPromptService promptService;
    private final int rateLimit;
    private final int historyLimit;
    private final int maxMessageLength;

    public AIChatService(ChatClient.Builder builder, AIConversationRepository conversations,
                         AIMessageRepository messages, AIUsageRateLimiter rateLimiter,
                         FleetFlowPromptService promptService,
                         @Value("${fleetflow.ai.max-requests-per-minute:10}") int rateLimit,
                         @Value("${fleetflow.ai.conversation-history-limit:10}") int historyLimit,
                         @Value("${fleetflow.ai.max-message-length:2000}") int maxMessageLength) {
        this.chatClient = builder.build(); this.conversations = conversations; this.messages = messages;
        this.rateLimiter = rateLimiter; this.promptService = promptService;
        this.rateLimit = rateLimit; this.historyLimit = Math.min(10, Math.max(0, historyLimit));
        this.maxMessageLength = Math.min(2000, Math.max(1, maxMessageLength));
    }

    public AIChatResponse chat(AIChatRequest request, String username) {
        if (request.message().length() > maxMessageLength) {
            throw new BadRequestException("message must be at most " + maxMessageLength + " characters");
        }
        rateLimiter.check(username, rateLimit);
        AIConversation conversation = loadOrCreate(request, username);
        List<AIMessage> history = messages.findTop10ByConversation_IdOrderByCreatedAtDesc(conversation.getId());
        Collections.reverse(history);
        // The submitted message is saved before the external call so failed requests retain the user's input.
        messages.save(new AIMessage(UUID.randomUUID(), conversation, MessageRole.USER, request.message().trim()));

        String transcript = history.stream().skip(Math.max(0, history.size() - historyLimit))
                .map(m -> m.getRole().name() + ": " + m.getContent())
                .reduce((a, b) -> a + "\n" + b).orElse("No previous messages.");
        String contextualPrompt = "Conversation history (untrusted context):\n" + transcript
                + "\n\nCurrent user message:\n" + request.message().trim();

        AIModelAnswer answer;
        try {
            answer = chatClient.prompt().system(promptService.getSystemPrompt()).user(contextualPrompt)
                    .call().entity(AIModelAnswer.class);
        } catch (Exception ex) {
            log.warn("AI provider request failed for conversation {}", conversation.getId());
            throw new AIProviderException("AI service is temporarily unavailable", ex);
        }
        if (answer == null || answer.answer() == null || answer.answer().isBlank()) {
            throw new AIProviderException("AI service returned an empty response", new IllegalStateException("Empty model response"));
        }
        String safeAnswer = answer.answer().trim();
        messages.save(new AIMessage(UUID.randomUUID(), conversation, MessageRole.ASSISTANT, safeAnswer));
        conversation.touch(); conversations.save(conversation);
        log.info("AI request completed. conversationId={}, category={}", conversation.getId(),
                answer.category() == null ? "uncategorized" : answer.category());
        return new AIChatResponse(conversation.getId(), safeAnswer, "SUCCESS");
    }

    private AIConversation loadOrCreate(AIChatRequest request, String username) {
        if (request.conversationId() == null) {
            String title = request.message().trim();
            int codePoints = title.codePointCount(0, title.length());
            if (codePoints > 200) title = title.substring(0, title.offsetByCodePoints(0, 197)) + "...";
            return conversations.save(new AIConversation(UUID.randomUUID(), username, title));
        }
        return conversations.findByIdAndUsername(request.conversationId(), username)
                .orElseThrow(() -> new AIConversationNotFoundException(request.conversationId()));
    }

    public record AIModelAnswer(String answer, String category) {}
}
