package com.fleetflow.billing.controller;

import com.fleetflow.billing.dto.BillingQuoteRequest;
import com.fleetflow.billing.dto.BillingQuoteResponse;
import com.fleetflow.billing.service.PricingService;
import com.fleetflow.billing.service.ShipmentBillingDataProvider;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing/quotes")
public class BillingQuoteController {

    private final PricingService pricingService;
    private final ShipmentBillingDataProvider shipmentProvider;

    public BillingQuoteController(
            PricingService pricingService,
            ShipmentBillingDataProvider shipmentProvider
    ) {
        this.pricingService = pricingService;
        this.shipmentProvider = shipmentProvider;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING', 'FLEET_MANAGER', 'CUSTOMER')")
    public BillingQuoteResponse calculateQuote(
            @Valid @RequestBody BillingQuoteRequest request
    ) {
        var shipment = shipmentProvider.getBillableShipment(
                request.shipmentId()
        );

        if (!shipment.eligibleForBilling()) {
            throw new IllegalStateException(
                    "Shipment is not eligible for billing"
            );
        }

        return pricingService.calculate(
                shipment.shipmentId(),
                shipment.distanceKm(),
                shipment.weightKg(),
                shipment.priority()
        );
    }
}