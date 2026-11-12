package com.fleetflow.ai.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AICustomerAccessRepository {
    private final JdbcTemplate jdbc;

    public AICustomerAccessRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<UUID> findCustomerId(UUID userId, UUID organizationId) {
        return jdbc.query("""
                SELECT customer_id FROM ai_customer_user_access
                WHERE user_id=? AND organization_id=?
                """, rs -> rs.next() ? Optional.of(rs.getObject(1, UUID.class)) : Optional.empty(),
                userId, organizationId);
    }

    public void grant(UUID userId, UUID customerId, UUID organizationId, UUID grantedBy) {
        jdbc.update("""
                INSERT INTO ai_customer_user_access(organization_id,user_id,customer_id,granted_by)
                VALUES (?,?,?,?)
                ON CONFLICT (organization_id,user_id) DO UPDATE
                SET customer_id=EXCLUDED.customer_id, granted_by=EXCLUDED.granted_by,
                    granted_at=CURRENT_TIMESTAMP
                """, organizationId, userId, customerId, grantedBy);
    }

    public int revoke(UUID userId, UUID organizationId) {
        return jdbc.update("DELETE FROM ai_customer_user_access WHERE user_id=? AND organization_id=?",
                userId, organizationId);
    }
}
