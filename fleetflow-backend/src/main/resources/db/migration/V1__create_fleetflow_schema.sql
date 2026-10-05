CREATE TABLE application_metadata (
    id BIGSERIAL PRIMARY KEY,

    application_name VARCHAR(100) NOT NULL,

    environment VARCHAR(30) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);