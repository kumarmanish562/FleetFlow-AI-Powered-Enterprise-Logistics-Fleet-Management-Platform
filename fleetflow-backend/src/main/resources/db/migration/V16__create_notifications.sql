CREATE TABLE notifications (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    type VARCHAR(50) NOT NULL,

    channel VARCHAR(30) NOT NULL,

    title VARCHAR(255) NOT NULL,

    message TEXT NOT NULL,

    status VARCHAR(30) NOT NULL,

    sent_at TIMESTAMPTZ,

    read_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_notification_channel
        CHECK (
            channel IN (
                'EMAIL',
                'SMS',
                'PUSH',
                'IN_APP'
            )
        ),

    CONSTRAINT chk_notification_status
        CHECK (
            status IN (
                'PENDING',
                'SENT',
                'FAILED',
                'READ'
            )
        )
);

CREATE INDEX idx_notifications_user
    ON notifications(user_id);

CREATE INDEX idx_notifications_status
    ON notifications(status);

CREATE INDEX idx_notifications_user_read
    ON notifications(user_id, read_at);