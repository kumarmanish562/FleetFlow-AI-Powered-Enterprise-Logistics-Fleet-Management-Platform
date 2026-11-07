CREATE TABLE notification_templates (
    id UUID PRIMARY KEY,

    notification_type VARCHAR(50) NOT NULL,

    channel VARCHAR(30) NOT NULL,

    locale VARCHAR(20) NOT NULL DEFAULT 'en',

    title_template VARCHAR(255) NOT NULL,

    message_template TEXT NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_notification_template
        UNIQUE (notification_type, channel, locale),

    CONSTRAINT chk_notification_template_channel
        CHECK (
            channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')
        )
);

CREATE INDEX idx_notification_templates_lookup
    ON notification_templates(
        notification_type,
        channel,
        locale,
        enabled
    );