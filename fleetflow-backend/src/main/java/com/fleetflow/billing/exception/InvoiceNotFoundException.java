package com.fleetflow.billing.exception;

import java.util.UUID;

public class InvoiceNotFoundException extends BillingException {

    public InvoiceNotFoundException(UUID invoiceId) {
        super("Invoice not found: " + invoiceId);
    }
}