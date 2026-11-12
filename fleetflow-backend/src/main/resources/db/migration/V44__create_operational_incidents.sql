CREATE TABLE operational_incidents (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    shipment_id UUID NOT NULL,
    category VARCHAR(40) NOT NULL CHECK (category IN ('ROUTE_DISRUPTION','VEHICLE_BREAKDOWN','DELIVERY_EXCEPTION','SAFETY','OTHER')),
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN','RESOLVED')),
    summary VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    reported_by UUID NOT NULL REFERENCES users(id),
    reported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMPTZ,
    CONSTRAINT fk_operational_incidents_shipment_org
        FOREIGN KEY (shipment_id, organization_id) REFERENCES shipments(id, organization_id)
);

CREATE INDEX idx_operational_incidents_org_time
    ON operational_incidents (organization_id, reported_at DESC);
CREATE INDEX idx_operational_incidents_org_status
    ON operational_incidents (organization_id, status, severity);
CREATE INDEX idx_operational_incidents_shipment
    ON operational_incidents (shipment_id, reported_at DESC);
