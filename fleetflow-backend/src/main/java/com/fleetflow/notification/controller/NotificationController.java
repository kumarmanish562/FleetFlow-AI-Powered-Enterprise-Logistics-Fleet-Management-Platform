
package com.fleetflow.notification.controller;


import com.fleetflow.notification.dto.NotificationPageResponse;
import com.fleetflow.notification.dto.NotificationResponse;
import com.fleetflow.notification.service.NotificationQueryService;
import com.fleetflow.notification.service.NotificationReadService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService queryService;
    private final NotificationReadService readService;

    @GetMapping
    public NotificationPageResponse list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size
    ) {
        return queryService.list(userId(authentication), page, size);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        return Map.of(
                "unreadCount",
                queryService.unreadCount(userId(authentication))
        );
    }

    @GetMapping("/{id}")
    public NotificationResponse get(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        return queryService.get(userId(authentication), id);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        return readService.markRead(userId(authentication), id);
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllRead(
            Authentication authentication
    ) {
        return Map.of(
                "updated",
                readService.markAllRead(userId(authentication))
        );
    }

    private UUID userId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication required"
            );
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Adapt userId() to your application's security principal",
                    exception
            );
        }
    }
}
