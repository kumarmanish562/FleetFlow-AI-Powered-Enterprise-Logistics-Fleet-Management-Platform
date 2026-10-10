ALTER TABLE routes
    ADD COLUMN shipment_id UUID,
    ADD COLUMN planned_departure_at TIMESTAMPTZ,
    ADD COLUMN estimated_arrival_at TIMESTAMPTZ;

ALTER TABLE routes
    ADD CONSTRAINT fk_routes_shipment
    FOREIGN KEY (shipment_id)
    REFERENCES shipments(id)
    ON DELETE RESTRICT;

CREATE UNIQUE INDEX uk_routes_shipment
    ON routes(shipment_id)
    WHERE shipment_id IS NOT NULL;

CREATE INDEX idx_routes_planned_departure
    ON routes(planned_departure_at);

ALTER TABLE routes
    ADD CONSTRAINT chk_routes_arrival_time
    CHECK (
        estimated_arrival_at IS NULL
        OR planned_departure_at IS NULL
        OR estimated_arrival_at >= planned_departure_at
    );