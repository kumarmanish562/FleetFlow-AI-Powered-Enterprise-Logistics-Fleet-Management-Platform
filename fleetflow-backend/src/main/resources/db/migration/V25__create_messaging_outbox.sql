CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,

    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(150) NOT NULL,

    event_type VARCHAR(150) NOT NULL,
    schema_version INTEGER NOT NULL DEFAULT 1,

    topic VARCHAR(250) NOT NULL,
    message_key VARCHAR(250) NOT NULL,
    payload TEXT NOT NULL,

    correlation_id VARCHAR(150),

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    locked_until TIMESTAMPTZ,
    claim_token UUID,
    last_error TEXT,

    CONSTRAINT chk_outbox_status
        CHECK (
            status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')
        ),

    CONSTRAINT chk_outbox_attempts
        CHECK (attempts >= 0),

    CONSTRAINT chk_outbox_schema_version
        CHECK (schema_version > 0)
);

CREATE INDEX idx_outbox_pending
    ON outbox_events (next_attempt_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_expired_claims
    ON outbox_events (locked_until)
    WHERE status = 'PROCESSING';

CREATE INDEX idx_outbox_failed
    ON outbox_events (created_at)
    WHERE status = 'FAILED';

CREATE INDEX idx_outbox_aggregate
    ON outbox_events (aggregate_type, aggregate_id, created_at);