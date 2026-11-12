package com.fleetflow.database;

import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
                "notifications",
                "organizations",
                "knowledge_documents",
                "knowledge_jobs",
                "vector_store",
                "ai_conversations",
                "ai_messages",
                "ai_message_feedback",
                "ai_message_sources",
                "ai_operational_events",
                "ai_customer_user_access",
                "ai_message_operational_evidence",
                "ai_operational_tool_audit",
                "ai_message_response_sections",
                "operational_incidents"
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

    @Test
    void knowledgeVectorDimensionMatchesConfiguredEmbeddingModel() {
        String format = jdbcTemplate.queryForObject("""
                SELECT format_type(a.atttypid, a.atttypmod)
                FROM pg_attribute a
                JOIN pg_class c ON c.oid = a.attrelid
                WHERE c.relname = 'vector_store' AND a.attname = 'embedding'
                """, String.class);
        assertThat(format).isEqualTo("vector(1536)");
    }

    @Test
    void assistantSchemaHasStableOwnerAndRetentionFields() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema='public' AND table_name='ai_conversations'
                  AND column_name IN ('user_id','organization_id','deleted_at','expires_at')
                """, Integer.class);
        assertThat(count).isEqualTo(4);

        Integer citationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema='public' AND table_name='ai_message_sources'
                  AND column_name IN ('message_id','document_id','chunk_id','page_number','similarity')
                """, Integer.class);
        assertThat(citationCount).isEqualTo(5);
    }

    @Test
    void phase20SchemaHasTenantOwnershipAndOperationalEvidence() {
        Integer tenantColumns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema='public' AND column_name='organization_id'
                  AND table_name IN ('vehicles','customers','shipments','routes','tracking_events',
                    'deliveries','maintenance_records','drivers','ai_operational_events')
                """, Integer.class);
        assertThat(tenantColumns).isEqualTo(9);

        Integer tenantConstraints = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.table_constraints
                WHERE constraint_schema='public' AND constraint_name IN (
                    'fk_shipments_customer_org','fk_shipments_vehicle_org','fk_shipments_driver_org',
                    'fk_routes_shipment_org','fk_tracking_shipment_org','fk_deliveries_shipment_org',
                    'fk_maintenance_vehicle_org','fk_drivers_assigned_vehicle_org',
                    'fk_operational_incidents_shipment_org')
                """, Integer.class);
        assertThat(tenantConstraints).isEqualTo(9);
    }
}
