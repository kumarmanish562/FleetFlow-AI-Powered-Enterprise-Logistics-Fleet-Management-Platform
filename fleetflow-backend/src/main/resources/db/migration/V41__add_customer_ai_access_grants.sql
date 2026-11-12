CREATE TABLE ai_customer_user_access (
    organization_id UUID NOT NULL REFERENCES organizations(id),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL,
    granted_by UUID NOT NULL REFERENCES users(id),
    granted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_customer_user_access PRIMARY KEY (organization_id, user_id),
    CONSTRAINT fk_ai_customer_user_access_customer
        FOREIGN KEY (customer_id, organization_id) REFERENCES customers(id, organization_id)
);

CREATE INDEX idx_ai_customer_user_access_customer
    ON ai_customer_user_access (organization_id, customer_id);
