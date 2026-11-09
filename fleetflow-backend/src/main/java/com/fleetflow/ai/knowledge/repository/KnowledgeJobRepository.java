package com.fleetflow.ai.knowledge.repository;

import com.fleetflow.ai.knowledge.model.KnowledgeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeJobRepository extends JpaRepository<KnowledgeJob, UUID> {
    Optional<KnowledgeJob> findByDocumentIdAndOrganizationId(UUID documentId, UUID organizationId);
}
