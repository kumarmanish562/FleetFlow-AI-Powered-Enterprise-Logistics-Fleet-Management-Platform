package com.fleetflow.notification.dto;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationStatus;
import com.fleetflow.notification.enums.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        NotificationChannel channel,
        String title,
        String message,
        NotificationStatus status,
        String referenceType,
        String referenceId,
        OffsetDateTime createdAt,
        OffsetDateTime sentAt,
        OffsetDateTime readAt,
        boolean unread
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getChannel(),
                n.getTitle(),
                n.getMessage(),
                n.getStatus(),
                n.getReferenceType(),
                n.getReferenceId(),
                n.getCreatedAt(),
                n.getSentAt(),
                n.getReadAt(),
                n.getReadAt() == null
        );
    }
}