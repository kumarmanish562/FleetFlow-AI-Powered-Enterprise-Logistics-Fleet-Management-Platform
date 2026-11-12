ALTER TABLE vehicles
    ADD COLUMN organization_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'
    REFERENCES organizations(id);

ALTER TABLE customers
    ADD COLUMN organization_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'
    REFERENCES organizations(id);

CREATE INDEX idx_vehicles_org_status ON vehicles(organization_id, status);
CREATE INDEX idx_customers_org_email ON customers(organization_id, lower(email));
CREATE UNIQUE INDEX uk_vehicles_id_org ON vehicles(id, organization_id);

-- Prevent a shipment from linking a customer in another organization. The
-- customer_id FK remains the owning tenant boundary for shipments and routes.
CREATE UNIQUE INDEX uk_customers_id_org ON customers(id, organization_id);
ALTER TABLE shipments ADD COLUMN organization_id UUID;
UPDATE shipments s SET organization_id = c.organization_id
FROM customers c WHERE c.id = s.customer_id;
ALTER TABLE shipments ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE shipments ALTER COLUMN organization_id SET DEFAULT '00000000-0000-0000-0000-000000000001';
CREATE UNIQUE INDEX uk_shipments_id_org ON shipments(id, organization_id);
ALTER TABLE shipments ADD CONSTRAINT fk_shipments_customer_org
    FOREIGN KEY (customer_id, organization_id) REFERENCES customers(id, organization_id);
CREATE INDEX idx_shipments_org_status ON shipments(organization_id, status);

ALTER TABLE routes ADD COLUMN organization_id UUID;
UPDATE routes r SET organization_id = s.organization_id FROM shipments s WHERE s.id = r.shipment_id;
ALTER TABLE routes ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE routes ADD CONSTRAINT fk_routes_shipment_org
    FOREIGN KEY (shipment_id, organization_id) REFERENCES shipments(id, organization_id);
CREATE INDEX idx_routes_org_status ON routes(organization_id, status);

ALTER TABLE tracking_events ADD COLUMN organization_id UUID;
UPDATE tracking_events t SET organization_id = s.organization_id FROM shipments s WHERE s.id = t.shipment_id;
ALTER TABLE tracking_events ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE tracking_events ADD CONSTRAINT fk_tracking_shipment_org
    FOREIGN KEY (shipment_id, organization_id) REFERENCES shipments(id, organization_id);
CREATE INDEX idx_tracking_events_org_time ON tracking_events(organization_id, event_time DESC);

ALTER TABLE deliveries ADD COLUMN organization_id UUID;
UPDATE deliveries d SET organization_id = s.organization_id FROM shipments s WHERE s.id = d.shipment_id;
ALTER TABLE deliveries ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE deliveries ADD CONSTRAINT fk_deliveries_shipment_org
    FOREIGN KEY (shipment_id, organization_id) REFERENCES shipments(id, organization_id);
CREATE INDEX idx_deliveries_org_time ON deliveries(organization_id, delivered_at);

ALTER TABLE maintenance_records ADD COLUMN organization_id UUID;
UPDATE maintenance_records m SET organization_id = v.organization_id FROM vehicles v WHERE v.id = m.vehicle_id;
ALTER TABLE maintenance_records ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE maintenance_records ADD CONSTRAINT fk_maintenance_vehicle_org
    FOREIGN KEY (vehicle_id, organization_id) REFERENCES vehicles(id, organization_id);
CREATE INDEX idx_maintenance_org_vehicle ON maintenance_records(organization_id, vehicle_id, maintenance_date DESC);

CREATE OR REPLACE FUNCTION fleetflow_assign_shipment_organization() RETURNS trigger AS $$
BEGIN
    SELECT organization_id INTO NEW.organization_id FROM shipments WHERE id = NEW.shipment_id;
    IF NEW.organization_id IS NULL THEN RAISE EXCEPTION 'Shipment organization could not be resolved'; END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fleetflow_assign_customer_organization() RETURNS trigger AS $$
BEGIN
    SELECT organization_id INTO NEW.organization_id FROM customers WHERE id = NEW.customer_id;
    IF NEW.organization_id IS NULL THEN RAISE EXCEPTION 'Customer organization could not be resolved'; END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_shipments_assign_organization BEFORE INSERT OR UPDATE OF customer_id
    ON shipments FOR EACH ROW EXECUTE FUNCTION fleetflow_assign_customer_organization();

CREATE TRIGGER trg_routes_assign_organization BEFORE INSERT OR UPDATE OF shipment_id
    ON routes FOR EACH ROW EXECUTE FUNCTION fleetflow_assign_shipment_organization();
CREATE TRIGGER trg_tracking_assign_organization BEFORE INSERT OR UPDATE OF shipment_id
    ON tracking_events FOR EACH ROW EXECUTE FUNCTION fleetflow_assign_shipment_organization();
CREATE TRIGGER trg_deliveries_assign_organization BEFORE INSERT OR UPDATE OF shipment_id
    ON deliveries FOR EACH ROW EXECUTE FUNCTION fleetflow_assign_shipment_organization();

CREATE OR REPLACE FUNCTION fleetflow_assign_vehicle_organization() RETURNS trigger AS $$
BEGIN
    SELECT organization_id INTO NEW.organization_id FROM vehicles WHERE id = NEW.vehicle_id;
    IF NEW.organization_id IS NULL THEN RAISE EXCEPTION 'Vehicle organization could not be resolved'; END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_maintenance_assign_organization BEFORE INSERT OR UPDATE OF vehicle_id
    ON maintenance_records FOR EACH ROW EXECUTE FUNCTION fleetflow_assign_vehicle_organization();
