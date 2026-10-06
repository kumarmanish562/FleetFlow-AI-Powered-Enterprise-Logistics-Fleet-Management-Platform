package com.fleetflow;

import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FleetFlowDatabaseIntegrationTest
        extends PostgresContainerTest {

    @Test
    void contextLoads() {
    }
}