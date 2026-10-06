CREATE TABLE invoices (
    id UUID PRIMARY KEY,

    shipment_id UUID NOT NULL,

    invoice_number VARCHAR(50) NOT NULL,

    subtotal NUMERIC(14,2) NOT NULL,
    tax_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(14,2) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    status VARCHAR(30) NOT NULL,

    issued_at TIMESTAMPTZ,
    due_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_invoices_number
        UNIQUE (invoice_number),

    CONSTRAINT fk_invoices_shipment
        FOREIGN KEY (shipment_id)
        REFERENCES shipments(id),

    CONSTRAINT chk_invoice_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT chk_invoice_tax
        CHECK (tax_amount >= 0),

    CONSTRAINT chk_invoice_discount
        CHECK (discount_amount >= 0),

    CONSTRAINT chk_invoice_total
        CHECK (total_amount >= 0),

    CONSTRAINT chk_invoice_status
        CHECK (
            status IN (
                'DRAFT',
                'ISSUED',
                'PARTIALLY_PAID',
                'PAID',
                'OVERDUE',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_invoices_shipment
    ON invoices(shipment_id);

CREATE INDEX idx_invoices_status
    ON invoices(status);

CREATE INDEX idx_invoices_due_at
    ON invoices(due_at);