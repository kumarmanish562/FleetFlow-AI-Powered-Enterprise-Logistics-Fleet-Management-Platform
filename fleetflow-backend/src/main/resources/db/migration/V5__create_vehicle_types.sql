CREATE TABLE vehicle_types (
    id UUID PRIMARY KEY,

    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),

    max_load_kg NUMERIC(12,2) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_vehicle_types_name
        UNIQUE (name),

    CONSTRAINT chk_vehicle_type_load
        CHECK (max_load_kg > 0)
);