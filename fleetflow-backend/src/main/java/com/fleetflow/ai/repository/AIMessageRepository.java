package com.fleetflow.ai.repository;

import com.fleetflow.ai.model.AIMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AIMessageRepository extends JpaRepository<AIMessage, UUID> {
    List<AIMessage> findTop10ByConversation_IdOrderByCreatedAtDesc(UUID conversationId);
}
