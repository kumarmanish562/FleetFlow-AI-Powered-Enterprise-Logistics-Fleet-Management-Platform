CREATE TABLE shipment_items (
    id UUID PRIMARY KEY,

    shipment_id UUID NOT NULL,

    description VARCHAR(255) NOT NULL,

    quantity INTEGER NOT NULL,

    unit_weight_kg NUMERIC(12,2),
    unit_price NUMERIC(14,2),

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_shipment_items_shipment
        FOREIGN KEY (shipment_id)
        REFERENCES shipments(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_shipment_item_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_shipment_item_weight
        CHECK (
            unit_weight_kg IS NULL
            OR unit_weight_kg >= 0
        ),

    CONSTRAINT chk_shipment_item_price
        CHECK (
            unit_price IS NULL
            OR unit_price >= 0
        )
);

CREATE INDEX idx_shipment_items_shipment
    ON shipment_items(shipment_id);