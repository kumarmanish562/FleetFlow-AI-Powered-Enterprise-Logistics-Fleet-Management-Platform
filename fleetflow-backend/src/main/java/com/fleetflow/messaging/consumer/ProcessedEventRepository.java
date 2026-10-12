package com.fleetflow.messaging.consumer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProcessedEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProcessedEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean claim(String consumerName, UUID eventId) {
        return jdbcTemplate.update("""
                INSERT INTO processed_events (
                    consumer_name,
                    event_id
                )
                VALUES (?, ?)
                ON CONFLICT (consumer_name, event_id)
                DO NOTHING
                """,
                consumerName,
                eventId
        ) == 1;
    }
}