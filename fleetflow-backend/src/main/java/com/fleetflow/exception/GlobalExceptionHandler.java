package com.fleetflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // DUPLICATE RESOURCE
    // ============================================================

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {

        return build(
                HttpStatus.CONFLICT,
                "CONFLICT",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    // ============================================================
    // RESOURCE NOT FOUND
    // ============================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {

        return build(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    // ============================================================
    // BAD CREDENTIALS
    // ============================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {

        return build(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Invalid email or password",
                request,
                List.of()
        );
    }

    // ============================================================
    // DISABLED ACCOUNT
    // ============================================================

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(
            DisabledException exception,
            HttpServletRequest request
    ) {

        return build(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "User account is inactive",
                request,
                List.of()
        );
    }

    // ============================================================
    // LOCKED ACCOUNT
    // ============================================================

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(
            LockedException exception,
            HttpServletRequest request
    ) {

        return build(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "User account is locked",
                request,
                List.of()
        );
    }

    // ============================================================
    // VALIDATION ERROR
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        List<ValidationError> errors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                new ValidationError(
                                        error.getField(),
                                        error.getDefaultMessage()
                                )
                        )
                        .toList();

        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                request,
                errors
        );
    }

    // ============================================================
    // GENERIC EXCEPTION
    // ============================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception exception,
            HttpServletRequest request
    ) {

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request,
                List.of()
        );
    }

    // ============================================================
    // RESPONSE BUILDER
    // ============================================================

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            List<ValidationError> validationErrors
    ) {

        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(
            SecurityException exception,
            HttpServletRequest request
    ) {

        return build(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                exception.getMessage(),
                request,
                List.of()
        );
    }

}