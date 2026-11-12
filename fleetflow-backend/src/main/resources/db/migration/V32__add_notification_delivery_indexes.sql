CREATE INDEX idx_notifications_user_created
    ON notifications(user_id, created_at DESC);

-- V16 already creates this index; keep the statement safe for clean installs.
CREATE INDEX IF NOT EXISTS idx_notifications_user_read
    ON notifications(user_id, read_at);

CREATE INDEX idx_notifications_pending_retry
    ON notifications(status, next_attempt_at, created_at)
    WHERE status IN ('PENDING', 'FAILED');
