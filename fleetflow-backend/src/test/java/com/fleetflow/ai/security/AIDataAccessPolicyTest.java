package com.fleetflow.ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AIDataAccessPolicyTest {
    private final KnowledgePrincipalService principals = mock(KnowledgePrincipalService.class);
    private final AICustomerAccessRepository customerAccess = mock(AICustomerAccessRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final Authentication authentication = mock(Authentication.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();
    private AIDataAccessPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AIDataAccessPolicy(principals, new ObjectMapper(), customerAccess, jdbc);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void internalToolUsesPrincipalOrganizationAndCapturesEvidenceProvenance() {
        when(principals.resolve(authentication)).thenReturn(principal("FLEET_MANAGER"));
        UUID vehicleId = UUID.randomUUID();
        policy.beginRequest();

        var result = policy.read(authentication, "getVehicleStatus", scope -> {
            assertEquals(userId, scope.userId());
            assertEquals(organizationId, scope.organizationId());
            return new VehicleEvidence(vehicleId.toString(), "AVAILABLE");
        });
        var evidence = policy.finishRequest();

        assertEquals("AVAILABLE", result.status());
        assertEquals(java.util.List.of("getVehicleStatus"), evidence.toolReferences());
        assertEquals(java.util.List.of(vehicleId.toString()), evidence.verifiedFacts().getFirst().referenceIds());
        assertEquals("OPERATIONAL_RECORD", evidence.verifiedFacts().getFirst().source());
        verify(jdbc).update(contains("INSERT INTO ai_operational_tool_audit"),
                any(), eq(organizationId), eq(userId), eq("getVehicleStatus"), eq("SUCCESS"));
    }

    @Test
    void grantedCustomerCanUseShipmentToolsButCannotUseFleetTools() {
        when(principals.resolve(authentication)).thenReturn(principal("CUSTOMER"));
        UUID customerId = UUID.randomUUID();
        when(customerAccess.findCustomerId(userId, organizationId)).thenReturn(Optional.of(customerId));

        assertEquals(customerId, policy.read(authentication, "getShipmentDetails", scope -> scope.customerId()));
        assertThrows(AccessDeniedException.class,
                () -> policy.read(authentication, "getVehicleStatus", scope -> "must not run"));
        assertEquals(Set.of("getShipmentDetails", "getShipmentTracking", "getDeliveryIncidents"),
                Set.copyOf(policy.availableToolNames(authentication)));
        verify(jdbc).update(contains("INSERT INTO ai_operational_tool_audit"),
                any(), eq(organizationId), eq(userId), eq("getVehicleStatus"), eq("DENIED"));
    }

    @Test
    void customerWithoutGrantCannotUseOperationalTools() {
        when(principals.resolve(authentication)).thenReturn(principal("CUSTOMER"));
        when(customerAccess.findCustomerId(userId, organizationId)).thenReturn(Optional.empty());

        assertFalse(policy.mayUseOperationalTools(authentication));
        assertThrows(AccessDeniedException.class,
                () -> policy.read(authentication, "getShipmentDetails", scope -> "must not run"));
        assertTrue(policy.availableToolNames(authentication).isEmpty());
    }

    private KnowledgePrincipalService.Principal principal(String role) {
        return new KnowledgePrincipalService.Principal(userId, organizationId, Set.of(role));
    }

    private record VehicleEvidence(String id, String status) {}
}
