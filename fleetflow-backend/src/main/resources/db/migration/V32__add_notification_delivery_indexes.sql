CREATE INDEX idx_notifications_user_created
    ON notifications(user_id, created_at DESC);

CREATE INDEX idx_notifications_user_read
    ON notifications(user_id, read_at);

CREATE INDEX idx_notifications_pending_retry
    ON notifications(status, next_attempt_at, created_at)
    WHERE status IN ('PENDING', 'FAILED');