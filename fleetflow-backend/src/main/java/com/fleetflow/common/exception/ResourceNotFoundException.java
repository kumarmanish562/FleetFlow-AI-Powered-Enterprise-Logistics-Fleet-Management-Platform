package com.fleetflow.common.exception;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(
            String message
    ) {

        super(message);
    }
}