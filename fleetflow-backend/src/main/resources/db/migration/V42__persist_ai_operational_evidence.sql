CREATE TABLE ai_message_operational_evidence (
    message_id UUID NOT NULL REFERENCES ai_messages(id) ON DELETE CASCADE,
    ordinal INTEGER NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    evidence JSONB NOT NULL,
    observed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (message_id, ordinal)
);

CREATE INDEX idx_ai_message_operational_evidence_tool
    ON ai_message_operational_evidence (tool_name, observed_at DESC);
