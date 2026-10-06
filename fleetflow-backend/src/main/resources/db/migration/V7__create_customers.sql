CREATE TABLE customers (
    id UUID PRIMARY KEY,

    name VARCHAR(200) NOT NULL,
    email VARCHAR(320),
    phone VARCHAR(30),

    customer_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    billing_address TEXT,
    shipping_address TEXT,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_customer_type
        CHECK (
            customer_type IN (
                'INDIVIDUAL',
                'BUSINESS'
            )
        ),

    CONSTRAINT chk_customer_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'BLOCKED'
            )
        )
);

CREATE INDEX idx_customers_email
    ON customers(email);

CREATE INDEX idx_customers_status
    ON customers(status);