CREATE TABLE payments (
    id UUID PRIMARY KEY,

    invoice_id UUID NOT NULL,

    payment_reference VARCHAR(100) NOT NULL,

    amount NUMERIC(14,2) NOT NULL,

    payment_method VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    paid_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_payments_reference
        UNIQUE (payment_reference),

    CONSTRAINT fk_payments_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    CONSTRAINT chk_payment_amount
        CHECK (amount > 0),

    CONSTRAINT chk_payment_method
        CHECK (
            payment_method IN (
                'CASH',
                'CARD',
                'UPI',
                'BANK_TRANSFER',
                'ONLINE'
            )
        ),

    CONSTRAINT chk_payment_status
        CHECK (
            status IN (
                'PENDING',
                'PROCESSING',
                'SUCCESS',
                'FAILED',
                'REFUNDED'
            )
        )
);

CREATE INDEX idx_payments_invoice
    ON payments(invoice_id);

CREATE INDEX idx_payments_status
    ON payments(status);