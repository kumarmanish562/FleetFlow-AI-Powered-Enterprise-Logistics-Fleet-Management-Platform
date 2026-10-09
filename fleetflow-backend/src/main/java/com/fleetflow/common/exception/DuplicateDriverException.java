package com.fleetflow.common.exception;

public class DuplicateDriverException
        extends RuntimeException {

    public DuplicateDriverException(
            String message
    ) {
        super(message);
    }
}