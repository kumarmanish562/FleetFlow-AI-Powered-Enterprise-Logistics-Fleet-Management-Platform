package com.fleetflow.ai.knowledge.repository;

import com.fleetflow.ai.knowledge.model.KnowledgeDocument;
import com.fleetflow.ai.knowledge.model.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {
    Optional<KnowledgeDocument> findByIdAndOrganizationId(UUID id, UUID organizationId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from KnowledgeDocument d where d.id = :id and d.organizationId = :organizationId")
    Optional<KnowledgeDocument> findLocked(UUID id, UUID organizationId);
    Page<KnowledgeDocument> findByOrganizationIdAndStatusNot(UUID organizationId, DocumentStatus status, Pageable page);
    List<KnowledgeDocument> findByOrganizationIdAndStatus(UUID organizationId, DocumentStatus status);
    boolean existsByOrganizationIdAndChecksumAndStatusNot(UUID organizationId, String checksum, DocumentStatus status);
}
