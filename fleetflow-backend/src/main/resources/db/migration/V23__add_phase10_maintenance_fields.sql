
-- Phase 10: Maintenance Management
-- Add fields required by MaintenanceRecord entity.

ALTER TABLE maintenance_records
    ADD COLUMN downtime_hours NUMERIC(8, 2),
    ADD COLUMN safety_critical BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN root_cause TEXT,
    ADD COLUMN parts_replaced TEXT,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
