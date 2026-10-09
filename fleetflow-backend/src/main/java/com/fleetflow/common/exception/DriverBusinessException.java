package com.fleetflow.common.exception;

public class DriverBusinessException
        extends RuntimeException {

    public DriverBusinessException(
            String message
    ) {
        super(message);
    }
}
