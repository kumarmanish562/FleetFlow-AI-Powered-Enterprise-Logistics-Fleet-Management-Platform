ALTER TABLE ai_conversations ADD COLUMN user_id UUID;

UPDATE ai_conversations c
SET user_id = u.id
FROM users u
WHERE LOWER(u.email) = LOWER(c.username);

ALTER TABLE ai_conversations
    ADD CONSTRAINT fk_ai_conversations_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE INDEX idx_ai_conversations_user_organization_updated
    ON ai_conversations(user_id, organization_id, updated_at DESC)
    WHERE deleted_at IS NULL;
