CREATE TABLE vehicles (
    id UUID PRIMARY KEY,

    vehicle_type_id UUID NOT NULL,

    registration_number VARCHAR(50) NOT NULL,
    vin VARCHAR(100),

    manufacturer VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,

    manufacture_year INTEGER,

    fuel_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    current_odometer NUMERIC(12,2) NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_vehicles_registration
        UNIQUE (registration_number),

    CONSTRAINT uk_vehicles_vin
        UNIQUE (vin),

    CONSTRAINT fk_vehicles_type
        FOREIGN KEY (vehicle_type_id)
        REFERENCES vehicle_types(id),

    CONSTRAINT chk_vehicle_odometer
        CHECK (current_odometer >= 0),

    CONSTRAINT chk_vehicle_status
        CHECK (
            status IN (
                'AVAILABLE',
                'ASSIGNED',
                'IN_TRANSIT',
                'MAINTENANCE',
                'OUT_OF_SERVICE',
                'RETIRED'
            )
        )
);

CREATE INDEX idx_vehicles_type
    ON vehicles(vehicle_type_id);

CREATE INDEX idx_vehicles_status
    ON vehicles(status);