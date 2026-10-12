package com.fleetflow.messaging.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class OutboxClaimRepository {

    private final JdbcTemplate jdbcTemplate;

    public OutboxClaimRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public List<OutboxClaim> claimBatch(int batchSize) {
        return jdbcTemplate.query("""
                WITH candidates AS (
                    SELECT id
                    FROM outbox_events
                    WHERE (
                        status = 'PENDING'
                        AND next_attempt_at <= CURRENT_TIMESTAMP
                    ) OR (
                        status = 'PROCESSING'
                        AND locked_until < CURRENT_TIMESTAMP
                    )
                    ORDER BY created_at, id
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?
                )
                UPDATE outbox_events AS o
                SET status = 'PROCESSING',
                    claim_token = gen_random_uuid(),
                    locked_until = CURRENT_TIMESTAMP
                        + INTERVAL '60 seconds'
                FROM candidates AS c
                WHERE o.id = c.id
                RETURNING o.id, o.event_id, o.topic,
                          o.message_key, o.payload, o.event_type,
                          o.aggregate_id, o.attempts, o.claim_token
                """,
                this::mapClaim,
                batchSize
        );
    }

    private OutboxClaim mapClaim(ResultSet rs, int row)
            throws SQLException {
        return new OutboxClaim(
                rs.getObject("id", UUID.class),
                rs.getObject("event_id", UUID.class),
                rs.getString("topic"),
                rs.getString("message_key"),
                rs.getString("payload"),
                rs.getString("event_type"),
                rs.getString("aggregate_id"),
                rs.getInt("attempts"),
                rs.getObject("claim_token", UUID.class)
        );
    }

    @Transactional
    public boolean markPublished(OutboxClaim claim) {
        return jdbcTemplate.update("""
                UPDATE outbox_events
                SET status = 'PUBLISHED',
                    published_at = CURRENT_TIMESTAMP,
                    locked_until = NULL,
                    claim_token = NULL,
                    last_error = NULL
                WHERE id = ?
                  AND status = 'PROCESSING'
                  AND claim_token = ?
                """,
                claim.id(),
                claim.claimToken()
        ) == 1;
    }

    @Transactional
    public void markFailure(
            OutboxClaim claim,
            String error,
            int maxAttempts
    ) {
        jdbcTemplate.update("""
                UPDATE outbox_events
                SET attempts = attempts + 1,
                    last_error = ?,
                    status = CASE
                        WHEN attempts + 1 >= ? THEN 'FAILED'
                        ELSE 'PENDING'
                    END,
                    next_attempt_at = CURRENT_TIMESTAMP
                        + (
                            LEAST(
                                300,
                                power(2, LEAST(attempts + 1, 8))
                            ) * INTERVAL '1 second'
                        ),
                    locked_until = NULL,
                    claim_token = NULL
                WHERE id = ?
                  AND status = 'PROCESSING'
                  AND claim_token = ?
                """,
                error,
                maxAttempts,
                claim.id(),
                claim.claimToken()
        );
    }
}