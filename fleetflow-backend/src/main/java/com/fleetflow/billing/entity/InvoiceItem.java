package com.fleetflow.billing.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "billing_invoice_items")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, updatable = false)
    private Invoice invoice;

    @Column(nullable = false, length = 40, updatable = false)
    private String code;

    @Column(nullable = false, length = 200, updatable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private boolean deduction;

    protected InvoiceItem() {
    }

    public InvoiceItem(
            String code,
            String description,
            BigDecimal amount,
            boolean deduction
    ) {
        if (code == null || code.isBlank()
                || description == null || description.isBlank()
                || amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Invalid invoice item");
        }

        this.code = code;
        this.description = description;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.deduction = deduction;
    }

    void attachTo(Invoice invoice) {
        if (this.invoice != null) {
            throw new IllegalStateException("Item is already attached");
        }
        this.invoice = invoice;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public boolean isDeduction() { return deduction; }
}