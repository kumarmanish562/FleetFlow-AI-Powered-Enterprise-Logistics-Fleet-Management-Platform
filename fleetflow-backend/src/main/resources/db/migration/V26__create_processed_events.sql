CREATE TABLE processed_events (
    consumer_name VARCHAR(150) NOT NULL,
    event_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_processed_events
        PRIMARY KEY (consumer_name, event_id)
);

CREATE INDEX idx_processed_events_processed_at
    ON processed_events (processed_at);