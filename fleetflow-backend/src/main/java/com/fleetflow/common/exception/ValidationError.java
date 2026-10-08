package com.fleetflow.common.exception;

public record ValidationError(
        String field,
        String message
) {
}