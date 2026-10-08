package com.fleetflow.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/security-test")
public class SecurityTestController {

    @GetMapping("/authenticated")
    public String authenticated() {

        return "Authenticated successfully";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {

        return "ADMIN access granted";
    }

    @GetMapping("/fleet")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER')"
    )
    public String fleet() {

        return "Fleet access granted";
    }

    @GetMapping("/dispatch")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DISPATCHER')"
    )
    public String dispatch() {

        return "Dispatcher access granted";
    }

    @GetMapping("/finance")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FINANCE_MANAGER')"
    )
    public String finance() {

        return "Finance access granted";
    }
}