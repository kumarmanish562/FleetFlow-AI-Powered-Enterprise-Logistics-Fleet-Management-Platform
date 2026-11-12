package com.fleetflow.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OperationalQuestionRequest(
        UUID conversationId,
        @NotBlank(message = "question is required")
        @Size(max = 2000, message = "question must be at most 2000 characters")
        String question
) {}
