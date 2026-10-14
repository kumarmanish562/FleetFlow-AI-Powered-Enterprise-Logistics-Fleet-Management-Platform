package com.fleetflow.billing.exception;

public class PaymentException extends BillingException {

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}