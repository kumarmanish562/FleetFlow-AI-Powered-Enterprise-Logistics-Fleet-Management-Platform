package com.fleetflow.billing.entity;

import com.fleetflow.billing.enums.InvoiceStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "billing_invoices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_billing_invoice_number",
                columnNames = "invoice_number"
        ),
        indexes = {
                @Index(name = "idx_invoice_customer", columnList = "customer_id"),
                @Index(name = "idx_invoice_shipment", columnList = "shipment_id"),
                @Index(name = "idx_invoice_status", columnList = "status")
        }
)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invoice_number", nullable = false, updatable = false, length = 40)
    private String invoiceNumber;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Column(name = "shipment_id", nullable = false, updatable = false)
    private Long shipmentId;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal discountAmount;

    @Column(name = "taxable_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal taxableAmount;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid = new BigDecimal("0.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "pricing_policy_version", nullable = false)
    private int pricingPolicyVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<InvoiceItem> items = new ArrayList<>();

    protected Invoice() {
    }

    public Invoice(
            String invoiceNumber,
            Long customerId,
            Long shipmentId,
            String currency,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal taxableAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            LocalDate dueDate,
            int pricingPolicyVersion
    ) {
        if (invoiceNumber == null || invoiceNumber.isBlank()
                || customerId == null || shipmentId == null
                || currency == null || !currency.matches("[A-Z]{3}")
                || dueDate == null || pricingPolicyVersion <= 0) {
            throw new IllegalArgumentException("Invalid invoice details");
        }

        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.shipmentId = shipmentId;
        this.currency = currency;
        this.subtotal = money(subtotal);
        this.discountAmount = money(discountAmount);
        this.taxableAmount = money(taxableAmount);
        this.taxAmount = money(taxAmount);
        this.totalAmount = money(totalAmount);
        this.dueDate = dueDate;
        this.pricingPolicyVersion = pricingPolicyVersion;

        if (this.discountAmount.compareTo(this.subtotal) > 0
                || this.taxableAmount.compareTo(
                this.subtotal.subtract(this.discountAmount)
        ) != 0
                || this.totalAmount.compareTo(
                this.taxableAmount.add(this.taxAmount)
        ) != 0) {
            throw new IllegalArgumentException(
                    "Invoice totals do not reconcile"
            );
        }
    }

    public void addItem(InvoiceItem item) {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Issued invoice items are immutable");
        }
        item.attachTo(this);
        items.add(item);
    }

    public void issue(LocalDate today) {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Invoice is not a draft");
        }
        if (today == null || dueDate.isBefore(today)) {
            throw new IllegalArgumentException("Invalid invoice due date");
        }
        issueDate = today;
        status = InvoiceStatus.ISSUED;
    }

    public void recordSuccessfulPayment(BigDecimal amount) {
        if (status != InvoiceStatus.ISSUED
                && status != InvoiceStatus.PARTIALLY_PAID
                && status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Invoice cannot accept payment");
        }

        BigDecimal value = money(amount);

        if (value.signum() <= 0 || value.compareTo(getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        amountPaid = money(amountPaid.add(value));
        status = amountPaid.compareTo(totalAmount) == 0
                ? InvoiceStatus.PAID
                : InvoiceStatus.PARTIALLY_PAID;
    }

    public BigDecimal getOutstandingBalance() {
        return money(totalAmount.subtract(amountPaid));
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Money value is required");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Long getCustomerId() { return customerId; }
    public Long getShipmentId() { return shipmentId; }
    public String getCurrency() { return currency; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public Instant getCreatedAt() { return createdAt; }
    public List<InvoiceItem> getItems() { return List.copyOf(items); }
    public int getPricingPolicyVersion() { return pricingPolicyVersion; }
}