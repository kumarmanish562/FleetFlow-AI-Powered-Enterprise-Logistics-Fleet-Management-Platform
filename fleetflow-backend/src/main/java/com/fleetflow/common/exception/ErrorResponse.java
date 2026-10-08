
        package com.fleetflow.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(

        OffsetDateTime timestamp,

        int status,

        String error,

        String message,

        String path,

        List<ValidationError> validationErrors
) {

    // ============================================================
    // CONSTRUCTOR WITHOUT VALIDATION ERRORS
    // ============================================================

    public ErrorResponse(
            int status,
            String error,
            String message,
            String path
    ) {

        this(
                OffsetDateTime.now(),
                status,
                error,
                message,
                path,
                List.of()
        );
    }

    // ============================================================
    // CONSTRUCTOR WITH VALIDATION ERRORS
    // ============================================================

    public ErrorResponse(
            int status,
            String error,
            String message,
            String path,
            List<ValidationError> validationErrors
    ) {

        this(
                OffsetDateTime.now(),
                status,
                error,
                message,
                path,
                validationErrors
        );
    }
}

