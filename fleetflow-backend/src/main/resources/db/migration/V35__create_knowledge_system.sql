-- Existing FleetFlow accounts are single tenant. Backfill them into a bootstrap
-- organization; later provisioning can assign users to distinct organizations.
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO organizations (id, name)
VALUES ('00000000-0000-0000-0000-000000000001', 'FleetFlow default organization');

ALTER TABLE users ADD COLUMN organization_id UUID NOT NULL
    DEFAULT '00000000-0000-0000-0000-000000000001'
    REFERENCES organizations(id);
CREATE INDEX idx_users_organization ON users(organization_id);

ALTER TABLE ai_conversations ADD COLUMN organization_id UUID;
UPDATE ai_conversations c SET organization_id = u.organization_id
FROM users u WHERE LOWER(u.email) = LOWER(c.username);
UPDATE ai_conversations SET organization_id = '00000000-0000-0000-0000-000000000001'
WHERE organization_id IS NULL;
ALTER TABLE ai_conversations ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE ai_conversations ALTER COLUMN organization_id
    SET DEFAULT '00000000-0000-0000-0000-000000000001';
ALTER TABLE ai_conversations ADD CONSTRAINT fk_ai_conversations_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);
CREATE INDEX idx_ai_conversations_owner_organization
    ON ai_conversations(username, organization_id);

CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    uploaded_by UUID NOT NULL REFERENCES users(id),
    filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    document_type VARCHAR(60) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('UPLOADED','PROCESSING','INDEXED','FAILED','DELETING','DELETED','SUPERSEDED')),
    checksum VARCHAR(64) NOT NULL,
    access_roles JSONB NOT NULL DEFAULT '[]'::jsonb,
    chunk_count INTEGER NOT NULL DEFAULT 0 CHECK (chunk_count >= 0),
    active_generation UUID,
    replaces_document_id UUID REFERENCES knowledge_documents(id),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    indexed_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);
CREATE INDEX idx_knowledge_documents_org_status ON knowledge_documents(organization_id, status);
CREATE UNIQUE INDEX uk_knowledge_documents_org_checksum_active
    ON knowledge_documents(organization_id, checksum) WHERE status <> 'DELETED';

CREATE TABLE knowledge_jobs (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL UNIQUE REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('INDEX','DELETE')),
    state VARCHAR(20) NOT NULL CHECK (state IN ('QUEUED','RUNNING','FAILED','DONE')),
    generation_id UUID,
    attempts INTEGER NOT NULL DEFAULT 0,
    run_after TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lease_until TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_knowledge_jobs_claim ON knowledge_jobs(state, run_after, lease_until);

-- Spring AI PGVectorStore owns inserts, embedding generation, and similarity search.
-- The table shape follows its documented schema; Flyway owns initialization.
CREATE TABLE vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT,
    metadata JSON,
    embedding VECTOR(1536)
);
CREATE INDEX idx_vector_store_embedding ON vector_store USING HNSW (embedding vector_cosine_ops);
CREATE INDEX idx_vector_store_metadata ON vector_store USING GIN ((metadata::jsonb));
