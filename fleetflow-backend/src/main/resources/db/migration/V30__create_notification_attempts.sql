CREATE TABLE notification_attempts (
    id UUID PRIMARY KEY,

    notification_id UUID NOT NULL,

    attempt_number INTEGER NOT NULL,

    provider VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL,

    provider_message_id VARCHAR(255),

    error_code VARCHAR(100),

    error_message TEXT,

    attempted_at TIMESTAMPTZ NOT NULL,

    completed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_notification_attempts_notification
        FOREIGN KEY (notification_id)
        REFERENCES notifications(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_notification_attempt_number
        UNIQUE (notification_id, attempt_number),

    CONSTRAINT chk_notification_attempt_number
        CHECK (attempt_number > 0),

    CONSTRAINT chk_notification_attempt_status
        CHECK (
            status IN (
                'STARTED',
                'SUCCEEDED',
                'RETRYABLE_FAILURE',
                'PERMANENT_FAILURE'
            )
        )
);

CREATE INDEX idx_notification_attempts_notification
    ON notification_attempts(notification_id, attempt_number DESC);