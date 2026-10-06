package com.fleetflow.database;

import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class DatabaseSchemaTest extends PostgresContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldHaveAllRequiredTables() {

        List<String> expectedTables = List.of(
                "users",
                "roles",
                "user_roles",
                "drivers",
                "vehicle_types",
                "vehicles",
                "customers",
                "routes",
                "shipments",
                "shipment_items",
                "deliveries",
                "tracking_events",
                "maintenance_records",
                "invoices",
                "payments",
                "notifications"
        );

        for (String table : expectedTables) {

            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_name = ?
                    """,
                    Integer.class,
                    table
            );

            assertThat(count)
                    .as("Table should exist: " + table)
                    .isEqualTo(1);
        }
    }

    @Test
    void shouldHaveFlywayHistoryTable() {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'flyway_schema_history'
                """,
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveVectorExtension() {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_extension
                WHERE extname = 'vector'
                """,
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }
}