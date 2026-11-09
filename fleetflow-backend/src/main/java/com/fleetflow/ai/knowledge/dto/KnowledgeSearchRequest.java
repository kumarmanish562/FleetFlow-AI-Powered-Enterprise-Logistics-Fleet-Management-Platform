package com.fleetflow.ai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeSearchRequest(@NotBlank @Size(max = 2000) String query) {}
