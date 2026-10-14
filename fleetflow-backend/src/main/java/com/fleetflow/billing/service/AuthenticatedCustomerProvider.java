
package com.fleetflow.billing.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedCustomerProvider {

    public Long requireCurrentCustomerId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException(
                    "An authenticated customer is required"
            );
        }

        Object principal = authentication.getPrincipal();

        // Adapt this section to your actual principal class.
        // Do not assume the authenticated user ID is the customer ID.
        throw new UnsupportedOperationException(
                "Extract the customer ID from the application's authenticated principal"
        );
    }
}
