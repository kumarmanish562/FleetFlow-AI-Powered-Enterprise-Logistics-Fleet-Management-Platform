package com.fleetflow.ai.knowledge;

import com.fleetflow.ai.knowledge.model.*;
import com.fleetflow.ai.knowledge.repository.KnowledgeDocumentRepository;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import com.fleetflow.ai.knowledge.service.KnowledgeRetrievalService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.Authentication;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeRetrievalServiceTest {
    private final KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
    private final KnowledgePrincipalService principals = mock(KnowledgePrincipalService.class);
    private final VectorStore vectors = mock(VectorStore.class);
    private final Authentication authentication = mock(Authentication.class);
    private final UUID org = UUID.randomUUID();
    private final KnowledgeRetrievalService service = new KnowledgeRetrievalService(documents, principals, vectors, 5, 0.7);

    @Test
    void restrictedRoleNeverCallsVectorStore() {
        when(principals.resolve(authentication)).thenReturn(new KnowledgePrincipalService.Principal(UUID.randomUUID(), org, Set.of("DRIVER")));
        when(documents.findByOrganizationIdAndStatus(org, DocumentStatus.INDEXED))
                .thenReturn(List.of(document(UUID.randomUUID(), List.of("FINANCE_MANAGER"))));

        assertTrue(service.retrieve("What is the finance policy?", authentication).isEmpty());
        verifyNoInteractions(vectors);
    }

    @Test
    void otherOrganizationNeverEntersVectorSearch() {
        when(principals.resolve(authentication)).thenReturn(new KnowledgePrincipalService.Principal(UUID.randomUUID(), org, Set.of("ADMIN")));
        when(documents.findByOrganizationIdAndStatus(org, DocumentStatus.INDEXED)).thenReturn(List.of());

        assertTrue(service.retrieve("What is the policy?", authentication).isEmpty());
        verifyNoInteractions(vectors);
    }

    @Test
    void authorizedSearchUsesMetadataFilterAndReturnsCitation() {
        UUID id = UUID.randomUUID();
        when(principals.resolve(authentication)).thenReturn(new KnowledgePrincipalService.Principal(UUID.randomUUID(), org, Set.of("DRIVER")));
        when(documents.findByOrganizationIdAndStatus(org, DocumentStatus.INDEXED))
                .thenReturn(List.of(document(id, List.of("DRIVER"))));
        Document chunk = new Document(UUID.randomUUID().toString(), "Report a breakdown to dispatch.", Map.of(
                "documentId", id.toString(), "organizationId", org.toString(), "filename", "handbook.pdf",
                "version", "1.0", "pageNumber", 4, "active", true));
        when(vectors.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(chunk));

        var sources = service.retrieve("What if the vehicle breaks down?", authentication);

        assertEquals(1, sources.size());
        assertEquals(id, sources.getFirst().documentId());
        assertEquals(4, sources.getFirst().pageNumber());
        verify(vectors).similaritySearch(argThat((SearchRequest request) -> request.getFilterExpression() != null));
    }

    private KnowledgeDocument document(UUID id, List<String> roles) {
        KnowledgeDocument d = new KnowledgeDocument(); d.setId(id); d.setOrganizationId(org);
        d.setStatus(DocumentStatus.INDEXED); d.setAccessRoles(roles); return d;
    }
}
