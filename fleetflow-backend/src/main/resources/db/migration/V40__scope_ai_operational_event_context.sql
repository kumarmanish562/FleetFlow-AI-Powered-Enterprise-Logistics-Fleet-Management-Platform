ALTER TABLE ai_operational_events
    ADD COLUMN organization_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'
    REFERENCES organizations(id);

CREATE INDEX idx_ai_operational_events_org_aggregate_time
    ON ai_operational_events (organization_id, aggregate_type, aggregate_id, occurred_at DESC);
