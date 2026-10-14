package com.fleetflow.billing.provider;

public record PaymentProviderResult(
        String providerReference,
        String checkoutUrl
) {
}