CREATE TABLE ai_operational_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_operational_events_aggregate_time
    ON ai_operational_events (aggregate_type, aggregate_id, occurred_at DESC);

CREATE INDEX idx_ai_operational_events_retention
    ON ai_operational_events (occurred_at);
