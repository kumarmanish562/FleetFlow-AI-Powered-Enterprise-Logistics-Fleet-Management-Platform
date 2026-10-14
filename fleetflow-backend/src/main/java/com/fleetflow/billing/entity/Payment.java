package com.fleetflow.billing.entity;

import com.fleetflow.billing.enums.PaymentMethod;
import com.fleetflow.billing.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "billing_payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_idempotency",
                        columnNames = {"customer_id", "idempotency_key"}
                ),
                @UniqueConstraint(
                        name = "uk_payment_provider_reference",
                        columnNames = {"provider", "provider_reference"}
                )
        },
        indexes = {
                @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, updatable = false)
    private Invoice invoice;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, length = 40, updatable = false)
    private String provider;

    @Column(name = "provider_reference", length = 150)
    private String providerReference;

    @Column(name = "idempotency_key", nullable = false, length = 100, updatable = false)
    private String idempotencyKey;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Payment() {
    }

    public Payment(
            Invoice invoice,
            Long customerId,
            BigDecimal amount,
            PaymentMethod method,
            String provider,
            String idempotencyKey
    ) {
        if (invoice == null || customerId == null
                || amount == null || amount.signum() <= 0
                || method == null || provider == null || provider.isBlank()
                || idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Invalid payment details");
        }

        if (!invoice.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Customer does not own invoice");
        }

        BigDecimal normalized = money(amount);

        if (normalized.compareTo(invoice.getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Payment exceeds outstanding balance");
        }

        this.invoice = invoice;
        this.customerId = customerId;
        this.amount = normalized;
        this.currency = invoice.getCurrency();
        this.method = method;
        this.provider = provider;
        this.idempotencyKey = idempotencyKey;
    }

    public void recordProviderReference(String reference) {
        requirePending();

        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Provider reference is required");
        }

        this.providerReference = reference;
    }

    public void markSuccessful(String verifiedReference, Instant confirmedAt) {
        requirePending();

        if (verifiedReference == null || verifiedReference.isBlank()
                || confirmedAt == null) {
            throw new IllegalArgumentException(
                    "Verified provider confirmation is required"
            );
        }

        if (providerReference != null
                && !providerReference.equals(verifiedReference)) {
            throw new IllegalStateException("Provider reference mismatch");
        }

        providerReference = verifiedReference;
        paidAt = confirmedAt;
        status = PaymentStatus.SUCCESS;
    }

    public void markFailed(String reason) {
        requirePending();
        status = PaymentStatus.FAILED;
        failureReason = reason == null
                ? "Payment failed"
                : reason.substring(0, Math.min(reason.length(), 500));
    }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not pending");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Invoice getInvoice() { return invoice; }
    public Long getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public String getProvider() { return provider; }
    public String getProviderReference() { return providerReference; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getFailureReason() { return failureReason; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCreatedAt() { return createdAt; }
}