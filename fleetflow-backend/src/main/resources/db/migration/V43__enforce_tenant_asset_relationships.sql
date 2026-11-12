ALTER TABLE drivers ADD COLUMN organization_id UUID;
UPDATE drivers d SET organization_id = u.organization_id FROM users u WHERE u.id = d.user_id;
ALTER TABLE drivers ALTER COLUMN organization_id SET NOT NULL;
CREATE UNIQUE INDEX uk_drivers_id_org ON drivers(id, organization_id);
CREATE INDEX idx_drivers_org_availability ON drivers(organization_id, employment_status, availability);

CREATE OR REPLACE FUNCTION fleetflow_assign_driver_organization() RETURNS trigger AS $$
BEGIN
    SELECT organization_id INTO NEW.organization_id FROM users WHERE id = NEW.user_id;
    IF NEW.organization_id IS NULL THEN RAISE EXCEPTION 'Driver organization could not be resolved'; END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_drivers_assign_organization BEFORE INSERT OR UPDATE OF user_id
    ON drivers FOR EACH ROW EXECUTE FUNCTION fleetflow_assign_driver_organization();

-- Existing global records can contain cross-organization assignments because
-- the earlier schema had no tenant boundary. Clear only those unsafe links;
-- the records themselves remain available for reassignment by their tenant.
UPDATE shipments s SET vehicle_id=NULL FROM vehicles v
WHERE s.vehicle_id=v.id AND s.organization_id<>v.organization_id;
UPDATE shipments s SET driver_id=NULL FROM drivers d
WHERE s.driver_id=d.id AND s.organization_id<>d.organization_id;
UPDATE drivers d SET assigned_vehicle_id=NULL FROM vehicles v
WHERE d.assigned_vehicle_id=v.id AND d.organization_id<>v.organization_id;

ALTER TABLE shipments ADD CONSTRAINT fk_shipments_vehicle_org
    FOREIGN KEY (vehicle_id, organization_id) REFERENCES vehicles(id, organization_id);
ALTER TABLE shipments ADD CONSTRAINT fk_shipments_driver_org
    FOREIGN KEY (driver_id, organization_id) REFERENCES drivers(id, organization_id);
ALTER TABLE drivers ADD CONSTRAINT fk_drivers_assigned_vehicle_org
    FOREIGN KEY (assigned_vehicle_id, organization_id) REFERENCES vehicles(id, organization_id);
