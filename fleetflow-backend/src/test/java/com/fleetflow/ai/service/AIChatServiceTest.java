package com.fleetflow.ai.service;

import com.fleetflow.ai.dto.request.AIChatRequest;
import com.fleetflow.ai.exception.AIConversationNotFoundException;
import com.fleetflow.ai.exception.AIProviderException;
import com.fleetflow.ai.exception.AIRateLimitException;
import com.fleetflow.ai.model.AIConversation;
import com.fleetflow.ai.model.AIMessage;
import com.fleetflow.ai.prompt.FleetFlowPromptService;
import com.fleetflow.ai.repository.AIConversationRepository;
import com.fleetflow.ai.repository.AIMessageRepository;
import com.fleetflow.ai.security.AIUsageRateLimiter;
import com.fleetflow.ai.knowledge.service.KnowledgeRetrievalService;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import com.fleetflow.user.entity.User;
import com.fleetflow.ai.knowledge.dto.KnowledgeSource;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AIChatServiceTest {
    private final ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
    private final ChatClient client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
    private final AIConversationRepository conversations = mock(AIConversationRepository.class);
    private final AIMessageRepository messages = mock(AIMessageRepository.class);
    private final AIUsageRateLimiter limiter = mock(AIUsageRateLimiter.class);
    private final KnowledgePrincipalService principals = mock(KnowledgePrincipalService.class);
    private AIChatService service;

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(client);
        service = new AIChatService(builder, conversations, messages, limiter,
                new FleetFlowPromptService(), 10, 10, 2000);
        service.setKnowledgePrincipalService(principals);
    }

    @Test
    void createsConversationAndPersistsBothMessages() {
        var answer = new AIChatService.AIModelAnswer("Preventive maintenance is planned service.", "maintenance");
        when(client.prompt().system(anyString()).user(anyString()).call().entity(AIChatService.AIModelAnswer.class))
                .thenReturn(answer);
        when(conversations.save(any(AIConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.chat(new AIChatRequest("What is preventive maintenance?", null), "dispatcher@example.com");

        assertEquals("SUCCESS", response.status());
        assertEquals(answer.answer(), response.answer());
        assertNotNull(response.conversationId());
        verify(messages, times(2)).save(any(AIMessage.class));
        verify(limiter).check("dispatcher@example.com", 10);
    }

    @Test
    void refusesConversationOwnedByAnotherUserAsNotFound() {
        UUID id = UUID.randomUUID();
        when(conversations.findByIdAndUsernameAndOrganizationId(id, "user@example.com", User.DEFAULT_ORGANIZATION_ID)).thenReturn(Optional.empty());

        assertThrows(AIConversationNotFoundException.class,
                () -> service.chat(new AIChatRequest("continue", id), "user@example.com"));
        verifyNoInteractions(messages);
    }

    @Test
    void authenticatedContinuationUsesStableUserAndOrganizationOwnership() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("renamed@example.com");
        when(principals.resolve(authentication)).thenReturn(new KnowledgePrincipalService.Principal(
                userId, User.DEFAULT_ORGANIZATION_ID, Set.of("DRIVER")));
        AIConversation conversation = new AIConversation(id, "old@example.com", userId,
                User.DEFAULT_ORGANIZATION_ID, "breakdown");
        when(conversations.findByIdAndUserIdAndOrganizationId(id, userId, User.DEFAULT_ORGANIZATION_ID))
                .thenReturn(Optional.of(conversation));
        when(messages.findTop10ByConversation_IdOrderByCreatedAtDesc(id)).thenReturn(List.of());
        when(client.prompt().system(anyString()).user(anyString()).call().entity(AIChatService.AIModelAnswer.class))
                .thenReturn(new AIChatService.AIModelAnswer("Check the handbook.", "policy"));

        service.chat(new AIChatRequest("continue", id), authentication);

        verify(conversations).findByIdAndUserIdAndOrganizationId(id, userId, User.DEFAULT_ORGANIZATION_ID);
        verify(conversations, never()).findByIdAndUsernameAndOrganizationId(any(), anyString(), any());
    }

    @Test
    void rejectsConfiguredMessageLimitBeforeProviderCall() {
        service = new AIChatService(builder, conversations, messages, limiter,
                new FleetFlowPromptService(), 10, 10, 12);
        assertThrows(com.fleetflow.common.exception.BadRequestException.class,
                () -> service.chat(new AIChatRequest("this message is too long", null), "user@example.com"));
        verifyNoInteractions(limiter, client, conversations, messages);
    }

    @Test
    void rateLimitExceptionIsPropagatedWithoutProviderCall() {
        doThrow(new AIRateLimitException("limited")).when(limiter).check("user@example.com", 10);
        assertThrows(AIRateLimitException.class,
                () -> service.chat(new AIChatRequest("hello", null), "user@example.com"));
        verifyNoInteractions(client, conversations, messages);
    }

    @Test
    void providerFailureIsMappedToSafeException() {
        when(conversations.save(any(AIConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(client.prompt().system(anyString()).user(anyString()).call().entity(AIChatService.AIModelAnswer.class))
                .thenThrow(new IllegalStateException("sensitive provider detail"));

        AIProviderException exception = assertThrows(AIProviderException.class,
                () -> service.chat(new AIChatRequest("hello", null), "user@example.com"));
        assertEquals("AI service is temporarily unavailable", exception.getMessage());
        assertFalse(exception.getMessage().contains("sensitive provider detail"));
    }

    @Test
    void usesBoundedChronologicalHistoryForContinuation() {
        UUID id = UUID.randomUUID();
        AIConversation conversation = new AIConversation(id, "user@example.com", "topic");
        when(conversations.findByIdAndUsernameAndOrganizationId(id, "user@example.com", User.DEFAULT_ORGANIZATION_ID)).thenReturn(Optional.of(conversation));
        when(messages.findTop10ByConversation_IdOrderByCreatedAtDesc(id)).thenReturn(List.of());
        when(client.prompt().system(anyString()).user(anyString()).call().entity(AIChatService.AIModelAnswer.class))
                .thenReturn(new AIChatService.AIModelAnswer("Follow-up response", "general"));

        var response = service.chat(new AIChatRequest("follow up", id), "user@example.com");

        assertEquals(id, response.conversationId());
        verify(messages).findTop10ByConversation_IdOrderByCreatedAtDesc(id);
        verify(conversations).save(conversation);
    }

    @Test
    void noAuthorizedContextReturnsSafeAnswerWithoutCallingModel() {
        KnowledgeRetrievalService knowledge = mock(KnowledgeRetrievalService.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");
        when(principals.resolve(authentication)).thenReturn(new KnowledgePrincipalService.Principal(UUID.randomUUID(), User.DEFAULT_ORGANIZATION_ID, Set.of("DRIVER")));
        when(knowledge.retrieve("What is the company policy?", authentication)).thenReturn(List.of());
        when(conversations.save(any(AIConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.setKnowledgeRetrievalService(knowledge);

        var response = service.chat(new AIChatRequest("What is the company policy?", null), authentication);

        assertTrue(response.answer().contains("could not find an authorized company document"));
        assertTrue(response.sources().isEmpty());
        verifyNoInteractions(client);
        verify(messages, times(2)).save(any(AIMessage.class));
    }

    @Test
    void groundedAnswerReturnsTraceableSource() {
        KnowledgeRetrievalService knowledge = mock(KnowledgeRetrievalService.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");
        when(principals.resolve(authentication)).thenReturn(new KnowledgePrincipalService.Principal(UUID.randomUUID(), User.DEFAULT_ORGANIZATION_ID, Set.of("DRIVER")));
        KnowledgeSource source = new KnowledgeSource(UUID.randomUUID(), "handbook.pdf", "2.0", 3,
                "Report a breakdown to dispatch.", 0.91);
        when(knowledge.retrieve("Breakdown procedure?", authentication)).thenReturn(List.of(source));
        when(conversations.save(any(AIConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(client.prompt().system(anyString()).user(anyString()).call().entity(AIChatService.AIModelAnswer.class))
                .thenReturn(new AIChatService.AIModelAnswer("Call dispatch [1].", "policy"));
        service.setKnowledgeRetrievalService(knowledge);

        var response = service.chat(new AIChatRequest("Breakdown procedure?", null), authentication);

        assertEquals("Call dispatch [1].", response.answer());
        assertEquals(source, response.sources().getFirst());
    }
}
