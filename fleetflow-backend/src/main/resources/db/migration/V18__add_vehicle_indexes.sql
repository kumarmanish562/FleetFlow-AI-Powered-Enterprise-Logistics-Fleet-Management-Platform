-- ============================================================
-- V18 - Additional Vehicle Indexes
-- ============================================================

-- Filter vehicles by fuel type
CREATE INDEX idx_vehicles_fuel_type
    ON vehicles(fuel_type);

-- Filter/search vehicles by manufacturer
CREATE INDEX idx_vehicles_manufacturer
    ON vehicles(manufacturer);

-- Filter/search vehicles by model
CREATE INDEX idx_vehicles_model
    ON vehicles(model);

-- Sort vehicles by creation time
CREATE INDEX idx_vehicles_created_at
    ON vehicles(created_at);

-- Common fleet-management query:
-- Find vehicles by status and vehicle type
CREATE INDEX idx_vehicles_status_type
    ON vehicles(status, vehicle_type_id);