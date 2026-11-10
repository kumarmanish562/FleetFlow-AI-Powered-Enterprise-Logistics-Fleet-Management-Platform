package com.fleetflow.ai.repository;

import com.fleetflow.ai.model.AIConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AIConversationRepository extends JpaRepository<AIConversation, UUID> {
    Optional<AIConversation> findByIdAndUsername(UUID id, String username);
    Optional<AIConversation> findByIdAndUsernameAndOrganizationId(UUID id, String username, UUID organizationId);
    Optional<AIConversation> findByIdAndUserIdAndOrganizationId(UUID id, UUID userId, UUID organizationId);
}
