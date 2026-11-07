package com.fleetflow.notification.dto;

import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;

import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceRequest(
        @NotNull NotificationType notificationType,
        @NotNull NotificationChannel channel,
        @NotNull Boolean enabled
) {
}