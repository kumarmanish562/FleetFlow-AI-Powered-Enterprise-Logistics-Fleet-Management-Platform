CREATE TABLE maintenance_records (
    id UUID PRIMARY KEY,

    vehicle_id UUID NOT NULL,

    maintenance_type VARCHAR(50) NOT NULL,

    description TEXT,

    maintenance_date DATE NOT NULL,

    odometer_reading NUMERIC(12,2),

    cost NUMERIC(14,2),

    next_service_date DATE,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_maintenance_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id),

    CONSTRAINT chk_maintenance_cost
        CHECK (
            cost IS NULL
            OR cost >= 0
        ),

    CONSTRAINT chk_maintenance_odometer
        CHECK (
            odometer_reading IS NULL
            OR odometer_reading >= 0
        )
);

CREATE INDEX idx_maintenance_vehicle
    ON maintenance_records(vehicle_id);

CREATE INDEX idx_maintenance_date
    ON maintenance_records(maintenance_date);

CREATE INDEX idx_maintenance_next_service
    ON maintenance_records(next_service_date);