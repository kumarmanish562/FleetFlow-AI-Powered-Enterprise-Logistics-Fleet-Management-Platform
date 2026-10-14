package com.fleetflow.billing.provider;

import java.math.BigDecimal;

public interface ShipmentBillingDataProvider {

    ShipmentBillingFacts getBillableShipment(Long shipmentId);

    record ShipmentBillingFacts(
            Long shipmentId,
            Long customerId,
            BigDecimal distanceKm,
            BigDecimal weightKg,
            String priority,
            boolean eligibleForBilling
    ) {
    }
}