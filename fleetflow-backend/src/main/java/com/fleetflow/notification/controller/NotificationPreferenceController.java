package com.fleetflow.notification.controller;

import com.fleetflow.notification.dto.NotificationPreferenceRequest;
import com.fleetflow.notification.dto.NotificationPreferenceResponse;
import com.fleetflow.notification.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping
    public List<NotificationPreferenceResponse> get(
            Authentication authentication
    ) {
        return preferenceService.getPreferences(userId(authentication));
    }

    @PutMapping
    public NotificationPreferenceResponse update(
            Authentication authentication,
            @Valid @RequestBody NotificationPreferenceRequest request
    ) {
        return preferenceService.update(userId(authentication), request);
    }

    private UUID userId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication required"
            );
        }

        return UUID.fromString(authentication.getName());
    }
}
