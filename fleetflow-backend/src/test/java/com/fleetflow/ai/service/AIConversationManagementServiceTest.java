package com.fleetflow.ai.service;

import com.fleetflow.ai.knowledge.model.DocumentStatus;
import com.fleetflow.ai.knowledge.model.KnowledgeDocument;
import com.fleetflow.ai.knowledge.repository.KnowledgeDocumentRepository;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import com.fleetflow.ai.knowledge.storage.KnowledgeFileStorage;
import com.fleetflow.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AIConversationManagementServiceTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final KnowledgePrincipalService principals = mock(KnowledgePrincipalService.class);
    private final KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
    private final KnowledgeFileStorage files = mock(KnowledgeFileStorage.class);
    private final AIConversationManagementService service = new AIConversationManagementService(jdbc, principals, documents, files);
    private final Authentication authentication = mock(Authentication.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn("changed-email@example.com");
        when(principals.resolve(authentication)).thenReturn(new KnowledgePrincipalService.Principal(userId, organizationId, Set.of("DRIVER")));
    }

    @Test
    void conversationListIsScopedByStableUserAndOrganizationIds() {
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());

        service.list(authentication);

        verify(jdbc).queryForList(contains("user_id=? AND organization_id=?"), eq(userId), eq(organizationId));
    }

    @Test
    void deleteDoesNotDiscloseConversationOwnedByAnotherUser() {
        UUID conversationId = UUID.randomUUID();
        when(jdbc.update(anyString(), any(), any(), any())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(conversationId, authentication));
        verify(jdbc).update(contains("user_id=? AND organization_id=?"), eq(conversationId), eq(userId), eq(organizationId));
    }

    @Test
    void sourceExcerptsAreFilteredAgainAgainstCurrentRoleAccess() {
        UUID documentId = UUID.randomUUID();
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(), any(), any())).thenReturn(1);
        when(jdbc.queryForList(contains("ai_message_sources"), any(Object[].class))).thenReturn(List.of(Map.of("documentId", documentId, "filename", "restricted.pdf")));
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(documentId); doc.setOrganizationId(organizationId); doc.setStatus(DocumentStatus.INDEXED);
        doc.setAccessRoles(List.of("FINANCE_MANAGER"));
        when(documents.findByIdAndOrganizationId(documentId, organizationId)).thenReturn(Optional.of(doc));

        Map<String,Object> result = service.sources(UUID.randomUUID(), authentication);

        assertTrue(((List<?>) result.get("sources")).isEmpty());
    }

    @Test
    void missingSourceDocumentIsReturnedAsNotFound() {
        UUID documentId = UUID.randomUUID();
        when(documents.findByIdAndOrganizationId(documentId, organizationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.openSource(documentId, authentication));
    }

    @Test
    void sourcePreviewRejectsDocumentOutsideCurrentRoleAccess() {
        UUID documentId = UUID.randomUUID();
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(documentId); doc.setOrganizationId(organizationId); doc.setStatus(DocumentStatus.INDEXED);
        doc.setStorageKey("org/document.pdf"); doc.setAccessRoles(List.of("FINANCE_MANAGER"));
        when(documents.findByIdAndOrganizationId(documentId, organizationId)).thenReturn(Optional.of(doc));

        assertThrows(ResourceNotFoundException.class, () -> service.openSource(documentId, authentication));
        verifyNoInteractions(files);
    }

    @Test
    void feedbackCannotBeSubmittedForAnotherUsersMessage() {
        UUID messageId = UUID.randomUUID();
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(), any(), any())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class,
                () -> service.feedback(messageId, "POSITIVE", null, authentication));
        verify(jdbc, never()).update(contains("ai_message_feedback"), any(), any(), any(), any());
    }
}
