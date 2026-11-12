package com.fleetflow.database;

import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TenantBoundaryIntegrationTest extends PostgresContainerTest {
    private static final UUID DEFAULT_ORGANIZATION =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shipmentCannotBeAssignedAVehicleOwnedByAnotherOrganization() {
        UUID otherOrganization = UUID.randomUUID();
        UUID vehicleTypeId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("INSERT INTO organizations(id,name) VALUES (?,?)", otherOrganization, "Tenant B");
        jdbc.update("""
                INSERT INTO vehicle_types(id,name,max_load_kg,created_at,updated_at)
                VALUES (?,?,?,?,?)
                """, vehicleTypeId, "Test type " + vehicleTypeId, new BigDecimal("1000.00"), now, now);
        jdbc.update("""
                INSERT INTO vehicles(id,vehicle_type_id,registration_number,manufacturer,model,fuel_type,
                                     status,current_odometer,created_at,updated_at,organization_id)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, vehicleId, vehicleTypeId, "TEST-" + vehicleId, "Test", "Van", "DIESEL",
                "AVAILABLE", BigDecimal.ZERO, now, now, DEFAULT_ORGANIZATION);
        jdbc.update("""
                INSERT INTO customers(id,name,customer_type,status,created_at,updated_at,organization_id)
                VALUES (?,?,?,?,?,?,?)
                """, customerId, "Tenant B customer", "BUSINESS", "ACTIVE", now, now, otherOrganization);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO shipments(id,customer_id,vehicle_id,shipment_number,status,priority,
                                      origin_address,destination_address,created_at,updated_at)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """, UUID.randomUUID(), customerId, vehicleId, "TEST-" + UUID.randomUUID(),
                "CREATED", "NORMAL", "Origin", "Destination", now, now))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
