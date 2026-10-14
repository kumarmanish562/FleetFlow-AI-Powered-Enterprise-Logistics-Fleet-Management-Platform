package com.fleetflow.billing.provider;

import java.math.BigDecimal;

public interface PaymentProvider {

    String providerName();

    PaymentProviderResult initiatePayment(
            String idempotencyKey,
            BigDecimal amount,
            String currency,
            String paymentMethod
    );
}