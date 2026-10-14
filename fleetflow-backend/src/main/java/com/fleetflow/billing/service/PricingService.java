package com.fleetflow.billing.service;

import com.fleetflow.billing.config.BillingProperties;
import com.fleetflow.billing.dto.BillingQuoteResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final BillingProperties properties;

    public PricingService(BillingProperties properties) {
        this.properties = properties;
        validateConfiguration();
    }

    public BillingQuoteResponse calculate(
            Long shipmentId,
            BigDecimal distanceKm,
            BigDecimal weightKg,
            String priority
    ) {
        if (shipmentId == null || shipmentId <= 0
                || distanceKm == null || distanceKm.signum() <= 0
                || weightKg == null || weightKg.signum() <= 0) {
            throw new IllegalArgumentException("Invalid shipment pricing inputs");
        }

        List<BillingQuoteResponse.Charge> charges = new ArrayList<>();

        BigDecimal base = money(properties.basePrice());
        BigDecimal distance = money(
                distanceKm.multiply(properties.pricePerKm())
        );
        BigDecimal weight = money(
                weightKg.multiply(properties.pricePerKg())
        );

        BigDecimal priorityCharge = switch (priority) {
            case "STANDARD" -> money(BigDecimal.ZERO);
            case "EXPRESS" -> money(properties.expressSurcharge());
            case "URGENT" -> money(properties.urgentSurcharge());
            default -> throw new IllegalArgumentException(
                    "Unsupported shipment priority"
            );
        };

        add(charges, "BASE_PRICE", "Base shipment price", base);
        add(charges, "DISTANCE", "Distance charge", distance);
        add(charges, "WEIGHT", "Weight charge", weight);
        add(charges, "PRIORITY", "Priority surcharge", priorityCharge);

        BigDecimal subtotal = money(
                base.add(distance).add(weight).add(priorityCharge)
        );

        BigDecimal fuel = percentage(
                subtotal,
                properties.fuelSurchargePercent()
        );

        add(charges, "FUEL_SURCHARGE", "Fuel surcharge", fuel);

        subtotal = money(subtotal.add(fuel));

        BigDecimal discount = percentage(
                subtotal,
                properties.discountPercent()
        );

        BigDecimal taxableAmount = money(subtotal.subtract(discount));

        BigDecimal tax = percentage(
                taxableAmount,
                properties.taxPercent()
        );

        BigDecimal total = money(taxableAmount.add(tax));

        return new BillingQuoteResponse(
                shipmentId,
                properties.currency(),
                List.copyOf(charges),
                subtotal,
                discount,
                taxableAmount,
                tax,
                total
        );
    }

    private BigDecimal percentage(
            BigDecimal amount,
            BigDecimal percent
    ) {
        validatePercent(percent);

        return money(
                amount.multiply(percent)
                        .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void add(
            List<BillingQuoteResponse.Charge> charges,
            String code,
            String description,
            BigDecimal amount
    ) {
        charges.add(
                new BillingQuoteResponse.Charge(code, description, amount)
        );
    }

    private void validateConfiguration() {
        if (properties.currency() == null
                || !properties.currency().matches("[A-Z]{3}")) {
            throw new IllegalStateException("Invalid billing currency");
        }

        validatePercent(properties.fuelSurchargePercent());
        validatePercent(properties.discountPercent());
        validatePercent(properties.taxPercent());
    }

    private void validatePercent(BigDecimal value) {
        if (value == null || value.signum() < 0
                || value.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException(
                    "Percentage must be between 0 and 100"
            );
        }
    }
}