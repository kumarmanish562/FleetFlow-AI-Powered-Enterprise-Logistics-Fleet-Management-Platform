package com.fleetflow.vehicle.repository;

import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VehicleIndexIntegrationTest
        extends PostgresContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void vehicleIndexes_shouldExist() {

        List<String> indexes =
                jdbcTemplate.queryForList(
                        """
                        SELECT indexname
                        FROM pg_indexes
                        WHERE tablename = 'vehicles'
                        """,
                        String.class
                );

        assertThat(indexes)
                .contains(
                        "idx_vehicles_type",
                        "idx_vehicles_status",
                        "idx_vehicles_fuel_type",
                        "idx_vehicles_manufacturer",
                        "idx_vehicles_model",
                        "idx_vehicles_created_at",
                        "idx_vehicles_status_type"
                );
    }
}