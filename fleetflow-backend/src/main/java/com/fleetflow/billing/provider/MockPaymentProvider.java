package com.fleetflow.billing.provider;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("dev")
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String providerName() {
        return "MOCK";
    }

    @Override
    public PaymentProviderResult initiatePayment(
            String idempotencyKey,
            BigDecimal amount,
            String currency,
            String paymentMethod
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key must not be blank"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be positive"
            );
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency must not be blank"
            );
        }

        // Development simulation only. No real payment is processed.
        String reference = "MOCK-" + UUID.randomUUID();

        return new PaymentProviderResult(
                reference,
                null
        );
    }
}
