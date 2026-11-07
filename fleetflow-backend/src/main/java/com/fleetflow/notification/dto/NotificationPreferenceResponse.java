package com.fleetflow.notification.dto;

import com.fleetflow.notification.entity.NotificationPreference;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        NotificationType notificationType,
        NotificationChannel channel,
        boolean enabled,
        OffsetDateTime consentAt
) {
    public static NotificationPreferenceResponse from(
            NotificationPreference preference
    ) {
        return new NotificationPreferenceResponse(
                preference.getId(),
                preference.getNotificationType(),
                preference.getChannel(),
                preference.isEnabled(),
                preference.getConsentAt()
        );
    }
}