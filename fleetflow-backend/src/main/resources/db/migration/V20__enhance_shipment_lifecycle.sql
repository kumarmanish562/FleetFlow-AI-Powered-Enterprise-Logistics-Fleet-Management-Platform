
-- Add delivery audit information to existing shipments.
-- Existing shipment records remain valid.

ALTER TABLE shipments
    ADD COLUMN delivered_at TIMESTAMPTZ,
    ADD COLUMN failure_reason VARCHAR(1000);

-- Supports recent-delivery queries.
CREATE INDEX idx_shipments_delivered_at
    ON shipments (delivered_at)
    WHERE delivered_at IS NOT NULL;

-- Supports shipment lookup by tracking/number.
-- The existing unique constraint on shipment_number is retained.
