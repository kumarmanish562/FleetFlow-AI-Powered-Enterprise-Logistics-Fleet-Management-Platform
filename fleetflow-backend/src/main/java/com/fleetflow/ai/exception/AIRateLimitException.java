package com.fleetflow.ai.exception;

public class AIRateLimitException extends RuntimeException {
    public AIRateLimitException(String message) { super(message); }
}
