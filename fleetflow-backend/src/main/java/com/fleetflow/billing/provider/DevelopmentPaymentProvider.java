package com.fleetflow.billing.provider;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("dev-payment")
public class DevelopmentPaymentProvider implements PaymentProvider {

    @Override
    public String providerName() {
        return "DEVELOPMENT";
    }

    @Override
    public PaymentProviderResult initiatePayment(
            String idempotencyKey,
            BigDecimal amount,
            String currency,
            String paymentMethod
    ) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Simulates creation of a payment attempt only.
        // It does not charge money and does not confirm payment.
        return new PaymentProviderResult(
                "DEV-" + UUID.randomUUID(),
                null
        );
    }
}