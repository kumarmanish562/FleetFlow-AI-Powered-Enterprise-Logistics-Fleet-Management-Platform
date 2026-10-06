CREATE TABLE tracking_events (
    id UUID PRIMARY KEY,

    shipment_id UUID NOT NULL,

    event_type VARCHAR(50) NOT NULL,

    event_time TIMESTAMPTZ NOT NULL,

    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),

    location_name VARCHAR(255),

    description TEXT,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_tracking_events_shipment
        FOREIGN KEY (shipment_id)
        REFERENCES shipments(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_tracking_events_shipment
    ON tracking_events(shipment_id);

CREATE INDEX idx_tracking_events_time
    ON tracking_events(event_time);

CREATE INDEX idx_tracking_events_shipment_time
    ON tracking_events(
        shipment_id,
        event_time DESC
    );