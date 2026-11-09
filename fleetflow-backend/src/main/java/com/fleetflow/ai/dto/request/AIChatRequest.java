package com.fleetflow.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AIChatRequest(
        @NotBlank(message = "message is required")
        @Size(max = 2000, message = "message must be at most 2000 characters")
        String message,
        UUID conversationId
) {}
