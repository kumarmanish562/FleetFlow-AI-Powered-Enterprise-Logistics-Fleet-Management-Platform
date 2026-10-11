package com.fleetflow.tracking.exception;

public class InvalidTrackingEventException
        extends RuntimeException {

    public InvalidTrackingEventException(String message) {
        super(message);
    }
}