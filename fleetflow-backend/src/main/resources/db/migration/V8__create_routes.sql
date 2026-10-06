CREATE TABLE routes (
    id UUID PRIMARY KEY,

    route_number VARCHAR(50) NOT NULL,

    origin_address TEXT NOT NULL,
    destination_address TEXT NOT NULL,

    distance_km NUMERIC(12,2),
    estimated_duration_minutes INTEGER,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_routes_number
        UNIQUE (route_number),

    CONSTRAINT chk_route_distance
        CHECK (
            distance_km IS NULL
            OR distance_km >= 0
        ),

    CONSTRAINT chk_route_duration
        CHECK (
            estimated_duration_minutes IS NULL
            OR estimated_duration_minutes > 0
        ),

    CONSTRAINT chk_route_status
        CHECK (
            status IN (
                'PLANNED',
                'ACTIVE',
                'COMPLETED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_routes_status
    ON routes(status);