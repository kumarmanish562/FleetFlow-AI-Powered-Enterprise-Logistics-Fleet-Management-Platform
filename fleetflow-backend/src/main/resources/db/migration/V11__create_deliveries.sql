CREATE TABLE deliveries (
    id UUID PRIMARY KEY,

    shipment_id UUID NOT NULL,

    status VARCHAR(30) NOT NULL,

    scheduled_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,

    recipient_name VARCHAR(200),

    delivery_notes TEXT,

    proof_of_delivery_url TEXT,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_deliveries_shipment
        UNIQUE (shipment_id),

    CONSTRAINT fk_deliveries_shipment
        FOREIGN KEY (shipment_id)
        REFERENCES shipments(id),

    CONSTRAINT chk_delivery_status
        CHECK (
            status IN (
                'PENDING',
                'IN_PROGRESS',
                'DELIVERED',
                'FAILED',
                'RESCHEDULED'
            )
        )
);

CREATE INDEX idx_deliveries_status
    ON deliveries(status);

CREATE INDEX idx_deliveries_scheduled
    ON deliveries(scheduled_at);