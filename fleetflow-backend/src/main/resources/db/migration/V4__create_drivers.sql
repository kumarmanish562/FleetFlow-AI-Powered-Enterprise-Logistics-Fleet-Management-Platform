CREATE TABLE drivers (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    license_number VARCHAR(100) NOT NULL,
    license_expiry_date DATE NOT NULL,
    license_type VARCHAR(50) NOT NULL,

    employment_status VARCHAR(30) NOT NULL,

    hire_date DATE,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_drivers_user
        UNIQUE (user_id),

    CONSTRAINT uk_drivers_license
        UNIQUE (license_number),

    CONSTRAINT fk_drivers_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT chk_driver_status
        CHECK (
            employment_status IN (
                'ACTIVE',
                'INACTIVE',
                'SUSPENDED',
                'TERMINATED'
            )
        )
);

CREATE INDEX idx_drivers_license_expiry
    ON drivers(license_expiry_date);