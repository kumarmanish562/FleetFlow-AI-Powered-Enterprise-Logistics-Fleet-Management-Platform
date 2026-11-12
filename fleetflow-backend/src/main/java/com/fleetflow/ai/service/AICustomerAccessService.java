package com.fleetflow.ai.service;

import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService;
import com.fleetflow.ai.security.AICustomerAccessRepository;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Service
public class AICustomerAccessService {
    private static final Logger log = LoggerFactory.getLogger(AICustomerAccessService.class);
    private final AICustomerAccessRepository access;
    private final KnowledgePrincipalService principals;
    private final CustomerRepository customers;
    private final UserRepository users;

    public AICustomerAccessService(AICustomerAccessRepository access, KnowledgePrincipalService principals,
                                  CustomerRepository customers, UserRepository users) {
        this.access = access;
        this.principals = principals;
        this.customers = customers;
        this.users = users;
    }

    @Transactional
    public void grant(UUID userId, UUID customerId, Authentication authentication) {
        var principal = principals.resolveAdmin(authentication);
        var customer = customers.findByIdAndOrganizationId(customerId, principal.organizationId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Customer not found"));
        var user = users.findById(userId)
                .filter(u -> principal.organizationId().equals(u.getOrganizationId()))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Customer user not found"));
        boolean customerRole = user.getRoles().stream().anyMatch(role -> "CUSTOMER".equals(role.getName()));
        if (!customerRole) throw new AccessDeniedException("AI customer access can only be granted to customer accounts");
        access.grant(user.getId(), customer.getId(), principal.organizationId(), principal.userId());
        log.info("AI customer access granted: organizationId={}, customerId={}, userId={}, grantedBy={}",
                principal.organizationId(), customer.getId(), user.getId(), principal.userId());
    }

    @Transactional
    public void revoke(UUID userId, Authentication authentication) {
        var principal = principals.resolveAdmin(authentication);
        access.revoke(userId, principal.organizationId());
        log.info("AI customer access revoked: organizationId={}, userId={}, revokedBy={}",
                principal.organizationId(), userId, principal.userId());
    }
}
