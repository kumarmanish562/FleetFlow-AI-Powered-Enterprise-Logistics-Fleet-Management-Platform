package com.fleetflow.tracking.exception;

import java.util.UUID;

public class TrackingEventNotFoundException
        extends RuntimeException {

    public TrackingEventNotFoundException(UUID eventId) {
        super("Tracking event not found: " + eventId);
    }
}