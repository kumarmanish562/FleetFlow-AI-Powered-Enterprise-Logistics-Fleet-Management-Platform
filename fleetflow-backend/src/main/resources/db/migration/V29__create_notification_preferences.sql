CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    notification_type VARCHAR(50) NOT NULL,

    channel VARCHAR(30) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    consent_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_notification_preferences_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_notification_preference
        UNIQUE (user_id, notification_type, channel),

    CONSTRAINT chk_notification_preference_channel
        CHECK (
            channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')
        )
);

CREATE INDEX idx_notification_preferences_user
    ON notification_preferences(user_id);