package com.fleetflow.ai.knowledge.security;

import com.fleetflow.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KnowledgePrincipalService {
    private final UserRepository users;
    public KnowledgePrincipalService(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public Principal resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        var user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));
        if (user.getOrganizationId() == null) throw new AccessDeniedException("Organization assignment required");
        Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toUnmodifiableSet());
        return new Principal(user.getId(), user.getOrganizationId(), roles);
    }

    public Principal resolveAdmin(Authentication authentication) {
        Principal principal = resolve(authentication);
        if (!principal.roles().contains("ADMIN") && !principal.roles().contains("SUPER_ADMIN")) {
            throw new AccessDeniedException("Knowledge administration requires an administrator role");
        }
        return principal;
    }

    public record Principal(UUID userId, UUID organizationId, Set<String> roles) {}
}
