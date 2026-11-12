CREATE TABLE ai_message_response_sections (
    message_id UUID PRIMARY KEY REFERENCES ai_messages(id) ON DELETE CASCADE,
    possible_explanations JSONB NOT NULL DEFAULT '[]'::jsonb,
    recommendations JSONB NOT NULL DEFAULT '[]'::jsonb,
    has_sufficient_evidence BOOLEAN NOT NULL DEFAULT FALSE
);
