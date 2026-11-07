-- Add the originating business event identifier.
ALTER TABLE notifications
    ADD COLUMN event_id UUID;

-- Add a deterministic key for duplicate-delivery prevention.
ALTER TABLE notifications
    ADD COLUMN delivery_key VARCHAR(150);

-- Store a related business entity, such as a shipment or invoice.
ALTER TABLE notifications
    ADD COLUMN reference_type VARCHAR(50);

ALTER TABLE notifications
    ADD COLUMN reference_id VARCHAR(100);

-- Retry metadata.
ALTER TABLE notifications
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE notifications
    ADD COLUMN next_attempt_at TIMESTAMPTZ;

-- Replace the original status constraint.
ALTER TABLE notifications
    DROP CONSTRAINT IF EXISTS chk_notification_status;

ALTER TABLE notifications
    ADD CONSTRAINT chk_notification_status
    CHECK (
        status IN (
            'PENDING',
            'PROCESSING',
            'SENT',
            'DELIVERED',
            'FAILED',
            'CANCELLED',
            'READ'
        )
    );

-- Preserve existing records while migrating the schema.
-- Legacy rows receive generated delivery keys.
UPDATE notifications
SET
    event_id = id,
    delivery_key = id::TEXT
WHERE event_id IS NULL
   OR delivery_key IS NULL;

ALTER TABLE notifications
    ALTER COLUMN event_id SET NOT NULL;

ALTER TABLE notifications
    ALTER COLUMN delivery_key SET NOT NULL;

ALTER TABLE notifications
    ADD CONSTRAINT uk_notification_delivery_key
    UNIQUE (delivery_key);

ALTER TABLE notifications
    ADD CONSTRAINT chk_notification_attempt_count
    CHECK (attempt_count >= 0);