package com.fleetflow.ai.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import com.fleetflow.ai.dto.response.OperationalFact;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import com.fleetflow.ai.security.AICustomerAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.function.Function;
import java.util.UUID;
import java.util.LinkedHashSet;

/**
 * Authorization boundary for read-only operational AI tools.
 *
 * Tenant and customer scope are resolved from authenticated server-side records.
 */
@Service
public class AIDataAccessPolicy {
    private static final Logger log = LoggerFactory.getLogger(AIDataAccessPolicy.class);
    private static final Set<String> OPERATIONAL_ROLES = Set.of(
            "ADMIN", "SUPER_ADMIN", "FLEET_MANAGER", "DISPATCHER");
    private static final Set<String> CUSTOMER_TOOLS = Set.of(
            "getShipmentDetails", "getShipmentTracking", "getDeliveryIncidents");

    private final KnowledgePrincipalService principals;
    private final ObjectMapper objectMapper;
    private final AICustomerAccessRepository customerAccess;
    private final JdbcTemplate jdbc;
    private final ThreadLocal<List<String>> toolTrace = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<OperationalFact>> operationalFacts = ThreadLocal.withInitial(ArrayList::new);

    public AIDataAccessPolicy(KnowledgePrincipalService principals, ObjectMapper objectMapper,
                              AICustomerAccessRepository customerAccess, JdbcTemplate jdbc) {
        this.principals = principals;
        this.objectMapper = objectMapper;
        this.customerAccess = customerAccess;
        this.jdbc = jdbc;
    }

    public boolean mayUseOperationalTools(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        try {
            AccessScope scope = resolveScope(authentication);
            return scope.roles().stream().anyMatch(OPERATIONAL_ROLES::contains) || scope.customerId() != null;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }

    public <T> T read(Authentication authentication, String toolName, Function<AccessScope, T> operation) {
        AccessScope scope = resolveScope(authentication);
        boolean internal = scope.roles().stream().anyMatch(OPERATIONAL_ROLES::contains);
        if (!internal && (scope.customerId() == null || !CUSTOMER_TOOLS.contains(toolName))) {
            audit(scope, toolName, "DENIED");
            log.warn("AI operational tool denied: tool={}, userId={}", toolName, scope.userId());
            throw new AccessDeniedException("Operational data access is not available for this account");
        }
        try {
            T result = operation.apply(scope);
            toolTrace.get().add(toolName);
            try {
                String value = objectMapper.writeValueAsString(result);
                JsonNode tree = objectMapper.readTree(value);
                operationalFacts.get().add(new OperationalFact(toolName, value, sourceType(toolName), Instant.now(),
                        extractReferenceIds(tree)));
            } catch (JsonProcessingException ex) {
                log.warn("AI operational result could not be captured for evidence: tool={}", toolName);
            }
            log.info("AI operational tool completed: tool={}, userId={}, organizationId={}",
                    toolName, scope.userId(), scope.organizationId());
            audit(scope, toolName, "SUCCESS");
            return result;
        } catch (RuntimeException ex) {
            audit(scope, toolName, "FAILED");
            log.warn("AI operational tool failed: tool={}, userId={}, reason={}",
                    toolName, scope.userId(), ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private String sourceType(String toolName) {
        return switch (toolName) {
            case "getDeliveryIncidents" -> "INCIDENT_RECORD";
            case "getShipmentTracking" -> "TRACKING_EVENT";
            case "getRouteAnalytics", "getFleetAnalytics", "getDeliveryPerformance" -> "ANALYTICS_RESULT";
            default -> "OPERATIONAL_RECORD";
        };
    }

    private List<String> extractReferenceIds(JsonNode root) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        collectReferenceIds(root, ids);
        return ids.stream().limit(100).toList();
    }

    private void collectReferenceIds(JsonNode node, LinkedHashSet<String> ids) {
        if (node == null || ids.size() >= 100) return;
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if ((key.equalsIgnoreCase("id") || key.toLowerCase(java.util.Locale.ROOT).endsWith("id"))
                        && value.isTextual()) {
                    String reference = value.asText();
                    if (reference.length() <= 80 && reference.matches("[A-Za-z0-9_-]+")) ids.add(reference);
                } else {
                    collectReferenceIds(value, ids);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                collectReferenceIds(item, ids);
                if (ids.size() >= 100) break;
            }
        }
    }

    public boolean customerOnly(Authentication authentication) {
        AccessScope scope = resolveScope(authentication);
        return scope.customerId() != null && scope.roles().stream().noneMatch(OPERATIONAL_ROLES::contains);
    }

    public List<String> availableToolNames(Authentication authentication) {
        AccessScope scope = resolveScope(authentication);
        if (scope.roles().stream().anyMatch(OPERATIONAL_ROLES::contains))
            return List.of("getVehicleStatus", "getUnavailableVehicles", "getDriverAvailability",
                    "getAvailableDrivers", "getShipmentDetails", "getShipmentTracking",
                    "getVehicleMaintenance", "getDeliveryIncidents", "getRouteAnalytics",
                    "getFleetAnalytics", "getDeliveryPerformance");
        if (scope.customerId() != null)
            return List.of("getShipmentDetails", "getShipmentTracking", "getDeliveryIncidents");
        return List.of();
    }

    private AccessScope resolveScope(Authentication authentication) {
        var principal = principals.resolve(authentication);
        UUID customerId = null;
        if (principal.roles().contains("CUSTOMER")) {
            customerId = customerAccess.findCustomerId(principal.userId(), principal.organizationId()).orElse(null);
        }
        return new AccessScope(principal.userId(), principal.organizationId(), principal.roles(), customerId);
    }

    public record AccessScope(UUID userId, UUID organizationId, Set<String> roles, UUID customerId) {}

    private void audit(AccessScope scope, String toolName, String outcome) {
        jdbc.update("""
                INSERT INTO ai_operational_tool_audit(id,organization_id,user_id,tool_name,outcome)
                VALUES (?,?,?,?,?)
                """, UUID.randomUUID(), scope.organizationId(), scope.userId(), toolName, outcome);
    }

    @Scheduled(cron = "0 25 * * * *")
    public void purgeExpiredAuditEntries() {
        jdbc.update("DELETE FROM ai_operational_tool_audit WHERE recorded_at < CURRENT_TIMESTAMP - INTERVAL '365 days'");
    }

    public void beginRequest() { toolTrace.remove(); operationalFacts.remove(); }

    public RequestEvidence finishRequest() {
        List<String> result = List.copyOf(toolTrace.get());
        List<OperationalFact> facts = List.copyOf(operationalFacts.get());
        toolTrace.remove();
        operationalFacts.remove();
        return new RequestEvidence(result, facts);
    }

    public record RequestEvidence(List<String> toolReferences, List<OperationalFact> verifiedFacts) {}
}
