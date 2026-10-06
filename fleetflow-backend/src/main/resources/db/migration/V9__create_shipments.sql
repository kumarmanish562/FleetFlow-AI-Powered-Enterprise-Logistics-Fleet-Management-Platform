CREATE TABLE shipments (
    id UUID PRIMARY KEY,

    customer_id UUID NOT NULL,
    driver_id UUID,
    vehicle_id UUID,
    route_id UUID,

    shipment_number VARCHAR(50) NOT NULL,

    status VARCHAR(40) NOT NULL,
    priority VARCHAR(20) NOT NULL,

    origin_address TEXT NOT NULL,
    destination_address TEXT NOT NULL,

    total_weight_kg NUMERIC(12,2),
    declared_value NUMERIC(14,2),

    pickup_date TIMESTAMPTZ,
    expected_delivery_date TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_shipments_number
        UNIQUE (shipment_number),

    CONSTRAINT fk_shipments_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_shipments_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id),

    CONSTRAINT fk_shipments_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id),

    CONSTRAINT fk_shipments_route
        FOREIGN KEY (route_id)
        REFERENCES routes(id),

    CONSTRAINT chk_shipment_status
        CHECK (
            status IN (
                'CREATED',
                'CONFIRMED',
                'PICKUP_SCHEDULED',
                'PICKED_UP',
                'IN_TRANSIT',
                'OUT_FOR_DELIVERY',
                'DELIVERED',
                'CANCELLED',
                'FAILED',
                'RETURNED'
            )
        ),

    CONSTRAINT chk_shipment_priority
        CHECK (
            priority IN (
                'LOW',
                'NORMAL',
                'HIGH',
                'URGENT'
            )
        ),

    CONSTRAINT chk_shipment_weight
        CHECK (
            total_weight_kg IS NULL
            OR total_weight_kg >= 0
        ),

    CONSTRAINT chk_shipment_value
        CHECK (
            declared_value IS NULL
            OR declared_value >= 0
        )
);

CREATE INDEX idx_shipments_customer
    ON shipments(customer_id);

CREATE INDEX idx_shipments_driver
    ON shipments(driver_id);

CREATE INDEX idx_shipments_vehicle
    ON shipments(vehicle_id);

CREATE INDEX idx_shipments_route
    ON shipments(route_id);

CREATE INDEX idx_shipments_status
    ON shipments(status);

CREATE INDEX idx_shipments_expected_delivery
    ON shipments(expected_delivery_date);