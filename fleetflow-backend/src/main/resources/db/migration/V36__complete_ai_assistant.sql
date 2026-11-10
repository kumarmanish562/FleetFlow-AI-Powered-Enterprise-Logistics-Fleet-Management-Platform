ALTER TABLE ai_conversations ADD COLUMN deleted_at TIMESTAMPTZ;
ALTER TABLE ai_conversations ADD COLUMN expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '90 days');
CREATE INDEX idx_ai_conversations_retention ON ai_conversations(expires_at) WHERE deleted_at IS NULL;

ALTER TABLE ai_messages ADD COLUMN retrieval_metadata JSONB NOT NULL DEFAULT '[]'::jsonb;

CREATE TABLE ai_message_feedback (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES ai_messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    feedback_type VARCHAR(20) NOT NULL CHECK (feedback_type IN ('POSITIVE','NEGATIVE')),
    comment VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ai_message_feedback_author UNIQUE (message_id, user_id)
);
CREATE INDEX idx_ai_feedback_created ON ai_message_feedback(created_at DESC);

CREATE TABLE ai_message_sources (
    message_id UUID NOT NULL REFERENCES ai_messages(id) ON DELETE CASCADE,
    ordinal INTEGER NOT NULL,
    document_id UUID NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    chunk_id UUID NOT NULL,
    filename VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    page_number INTEGER,
    excerpt TEXT NOT NULL,
    similarity DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (message_id, ordinal)
);
CREATE INDEX idx_ai_message_sources_document ON ai_message_sources(document_id);

CREATE OR REPLACE FUNCTION purge_expired_ai_conversations() RETURNS INTEGER
LANGUAGE plpgsql AS $$
DECLARE removed INTEGER;
BEGIN
  DELETE FROM ai_conversations WHERE expires_at < CURRENT_TIMESTAMP OR deleted_at < CURRENT_TIMESTAMP - INTERVAL '30 days';
  GET DIAGNOSTICS removed = ROW_COUNT;
  RETURN removed;
END;
$$;
