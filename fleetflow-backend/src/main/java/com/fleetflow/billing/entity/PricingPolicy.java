package com.fleetflow.billing.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "billing_pricing_policies",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pricing_policy_version",
                columnNames = {"policy_name", "version"}
        )
)
public class PricingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_name", nullable = false, length = 80)
    private String policyName;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal pricePerKm;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal pricePerKg;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal expressSurcharge;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal urgentSurcharge;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal fuelSurchargePercent;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal discountPercent;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal taxPercent;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected PricingPolicy() {
    }

    public PricingPolicy(
            String policyName,
            int version,
            String currency,
            BigDecimal basePrice,
            BigDecimal pricePerKm,
            BigDecimal pricePerKg,
            BigDecimal expressSurcharge,
            BigDecimal urgentSurcharge,
            BigDecimal fuelSurchargePercent,
            BigDecimal discountPercent,
            BigDecimal taxPercent
    ) {
        if (policyName == null || policyName.isBlank()
                || version <= 0
                || currency == null
                || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Invalid pricing policy identity");
        }

        requireNonNegative(basePrice, "basePrice");
        requireNonNegative(pricePerKm, "pricePerKm");
        requireNonNegative(pricePerKg, "pricePerKg");
        requireNonNegative(expressSurcharge, "expressSurcharge");
        requireNonNegative(urgentSurcharge, "urgentSurcharge");
        requirePercent(fuelSurchargePercent, "fuelSurchargePercent");
        requirePercent(discountPercent, "discountPercent");
        requirePercent(taxPercent, "taxPercent");

        this.policyName = policyName;
        this.version = version;
        this.currency = currency;
        this.basePrice = basePrice;
        this.pricePerKm = pricePerKm;
        this.pricePerKg = pricePerKg;
        this.expressSurcharge = expressSurcharge;
        this.urgentSurcharge = urgentSurcharge;
        this.fuelSurchargePercent = fuelSurchargePercent;
        this.discountPercent = discountPercent;
        this.taxPercent = taxPercent;
        this.active = false;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    private static void requireNonNegative(
            BigDecimal value,
            String name
    ) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }

    private static void requirePercent(
            BigDecimal value,
            String name
    ) {
        if (value == null
                || value.signum() < 0
                || value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(name + " must be 0..100");
        }
    }

    public UUID getId() { return id; }
    public String getPolicyName() { return policyName; }
    public int getVersion() { return version; }
    public String getCurrency() { return currency; }
    public BigDecimal getBasePrice() { return basePrice; }
    public BigDecimal getPricePerKm() { return pricePerKm; }
    public BigDecimal getPricePerKg() { return pricePerKg; }
    public BigDecimal getExpressSurcharge() { return expressSurcharge; }
    public BigDecimal getUrgentSurcharge() { return urgentSurcharge; }
    public BigDecimal getFuelSurchargePercent() { return fuelSurchargePercent; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}