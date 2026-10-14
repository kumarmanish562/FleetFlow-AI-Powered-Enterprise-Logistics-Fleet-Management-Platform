CREATE TABLE billing_pricing_policies (
    id UUID PRIMARY KEY,
    policy_name VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL CHECK (version > 0),
    currency VARCHAR(3) NOT NULL,
    base_price NUMERIC(19, 2) NOT NULL CHECK (base_price >= 0),
    price_per_km NUMERIC(19, 4) NOT NULL CHECK (price_per_km >= 0),
    price_per_kg NUMERIC(19, 4) NOT NULL CHECK (price_per_kg >= 0),
    express_surcharge NUMERIC(19, 2) NOT NULL CHECK (express_surcharge >= 0),
    urgent_surcharge NUMERIC(19, 2) NOT NULL CHECK (urgent_surcharge >= 0),
    fuel_surcharge_percent NUMERIC(7, 4) NOT NULL
        CHECK (fuel_surcharge_percent BETWEEN 0 AND 100),
    discount_percent NUMERIC(7, 4) NOT NULL
        CHECK (discount_percent BETWEEN 0 AND 100),
    tax_percent NUMERIC(7, 4) NOT NULL
        CHECK (tax_percent BETWEEN 0 AND 100),
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_pricing_policy_version UNIQUE(policy_name, version)
);

CREATE TABLE billing_invoices (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    shipment_id BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL CHECK (subtotal >= 0),
    discount_amount NUMERIC(19, 2) NOT NULL CHECK (discount_amount >= 0),
    taxable_amount NUMERIC(19, 2) NOT NULL CHECK (taxable_amount >= 0),
    tax_amount NUMERIC(19, 2) NOT NULL CHECK (tax_amount >= 0),
    total_amount NUMERIC(19, 2) NOT NULL CHECK (total_amount >= 0),
    amount_paid NUMERIC(19, 2) NOT NULL DEFAULT 0
        CHECK (amount_paid >= 0 AND amount_paid <= total_amount),
    status VARCHAR(24) NOT NULL,
    issue_date DATE,
    due_date DATE NOT NULL,
    pricing_policy_version INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_invoice_total
        CHECK (total_amount = taxable_amount + tax_amount),
    CONSTRAINT chk_invoice_taxable
        CHECK (taxable_amount = subtotal - discount_amount)
);

CREATE INDEX idx_invoice_customer
    ON billing_invoices(customer_id);

CREATE INDEX idx_invoice_shipment
    ON billing_invoices(shipment_id);

CREATE INDEX idx_invoice_status
    ON billing_invoices(status);

CREATE TABLE billing_invoice_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES billing_invoices(id),
    code VARCHAR(40) NOT NULL,
    description VARCHAR(200) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    deduction BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_invoice_item_invoice
    ON billing_invoice_items(invoice_id);

CREATE TABLE billing_payments (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES billing_invoices(id),
    customer_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    method VARCHAR(24) NOT NULL,
    status VARCHAR(24) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    provider_reference VARCHAR(150),
    idempotency_key VARCHAR(100) NOT NULL,
    failure_reason VARCHAR(500),
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_payment_idempotency
        UNIQUE(customer_id, idempotency_key),
    CONSTRAINT uk_payment_provider_reference
        UNIQUE(provider, provider_reference)
);

CREATE INDEX idx_payment_invoice
    ON billing_payments(invoice_id);

CREATE INDEX idx_payment_status
    ON billing_payments(status);