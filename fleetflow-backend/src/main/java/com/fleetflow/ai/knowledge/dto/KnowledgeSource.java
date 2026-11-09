package com.fleetflow.ai.knowledge.dto;

import java.util.UUID;

public record KnowledgeSource(UUID documentId, String filename, String version,
                              Integer pageNumber, String excerpt, Double similarity) {}
