package com.fleetflow.user.service;

import com.fleetflow.user.entity.User;
import com.fleetflow.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CurrentOrganizationProvider {
    private final UserRepository users;

    public CurrentOrganizationProvider(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public UUID requireOrganizationId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken)
            throw new AccessDeniedException("Authenticated organization required");
        User user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
        if (user.getOrganizationId() == null) throw new AccessDeniedException("Organization assignment required");
        return user.getOrganizationId();
    }
}
