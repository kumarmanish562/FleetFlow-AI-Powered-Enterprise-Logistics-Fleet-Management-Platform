-- ============================================================
-- V19__enhance_driver_management.sql
-- FleetFlow Phase 5 - Driver Management
-- ============================================================

ALTER TABLE drivers
    ADD COLUMN first_name VARCHAR(100),
    ADD COLUMN last_name VARCHAR(100),
    ADD COLUMN phone VARCHAR(20),
    ADD COLUMN date_of_birth DATE,
    ADD COLUMN years_of_experience INTEGER,
    ADD COLUMN availability VARCHAR(30),
    ADD COLUMN assigned_vehicle_id UUID;


-- ------------------------------------------------------------
-- Existing data migration
-- ------------------------------------------------------------

-- IMPORTANT:
-- If existing driver records exist, populate these fields
-- with real business data before making them NOT NULL.

-- ------------------------------------------------------------
-- Constraints
-- ------------------------------------------------------------

ALTER TABLE drivers
    ADD CONSTRAINT chk_drivers_experience
    CHECK (
        years_of_experience >= 0
    );

ALTER TABLE drivers
    ADD CONSTRAINT chk_drivers_availability
    CHECK (
        availability IN (
            'AVAILABLE',
            'ON_DUTY',
            'ON_DELIVERY',
            'OFF_DUTY',
            'ON_LEAVE'
        )
    );

ALTER TABLE drivers
    ADD CONSTRAINT chk_drivers_license_type
    CHECK (
        license_type IN (
            'LIGHT_MOTOR_VEHICLE',
            'HEAVY_MOTOR_VEHICLE',
            'COMMERCIAL',
            'TRANSPORT'
        )
    );


-- ------------------------------------------------------------
-- Driver -> Vehicle relationship
-- ------------------------------------------------------------

ALTER TABLE drivers
    ADD CONSTRAINT fk_drivers_vehicle
    FOREIGN KEY (assigned_vehicle_id)
    REFERENCES vehicles(id)
    ON DELETE SET NULL;


-- ------------------------------------------------------------
-- Indexes
-- ------------------------------------------------------------

CREATE INDEX idx_drivers_employment_status
    ON drivers(employment_status);

CREATE INDEX idx_drivers_availability
    ON drivers(availability);



CREATE INDEX idx_drivers_assigned_vehicle
    ON drivers(assigned_vehicle_id);

CREATE INDEX idx_drivers_last_name
    ON drivers(last_name);