package com.fleetflow.exception;

public record ValidationError(
        String field,
        String message
) {
}