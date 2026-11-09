package com.fleetflow.ai.dto.response;

import java.util.UUID;

public record AIChatResponse(UUID conversationId, String answer, String status) {}
