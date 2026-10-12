
-- Align the maintenance table with MaintenanceRecord entity.

ALTER TABLE maintenance_records
    RENAME COLUMN odometer_reading TO mileage;

ALTER TABLE maintenance_records
    ALTER COLUMN mileage TYPE BIGINT
    USING mileage::BIGINT;

ALTER TABLE maintenance_records
    ADD COLUMN IF NOT EXISTS downtime_hours NUMERIC(8, 2);

ALTER TABLE maintenance_records
    ADD COLUMN IF NOT EXISTS safety_critical BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE maintenance_records
    ADD COLUMN IF NOT EXISTS root_cause TEXT;

ALTER TABLE maintenance_records
    ADD COLUMN IF NOT EXISTS parts_replaced TEXT;

ALTER TABLE maintenance_records
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
