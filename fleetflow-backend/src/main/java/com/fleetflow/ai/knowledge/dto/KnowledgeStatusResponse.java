package com.fleetflow.ai.knowledge.dto;

import com.fleetflow.ai.knowledge.model.DocumentStatus;
import com.fleetflow.ai.knowledge.model.KnowledgeJob;
import java.util.UUID;

public record KnowledgeStatusResponse(UUID documentId, DocumentStatus documentStatus,
                                      KnowledgeJob.State jobState, int attempts, String errorMessage) {}
