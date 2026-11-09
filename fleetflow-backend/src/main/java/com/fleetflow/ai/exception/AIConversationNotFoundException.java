package com.fleetflow.ai.exception;

import java.util.UUID;

public class AIConversationNotFoundException extends RuntimeException {
    public AIConversationNotFoundException(UUID id) { super("Conversation not found: " + id); }
}
