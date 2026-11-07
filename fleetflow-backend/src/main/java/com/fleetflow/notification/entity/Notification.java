package com.fleetflow.notification.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationStatus;
import com.fleetflow.notification.enums.NotificationType;
import com.fleetflow.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notifications_user_created",
                        columnList = "user_id, created_at"
                ),
                @Index(
                        name = "idx_notifications_user_read",
                        columnList = "user_id, read_at"
                ),
                @Index(
                        name = "idx_notifications_status_retry",
                        columnList = "status, next_attempt_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_delivery_key",
                        columnNames = "delivery_key"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "delivery_key", nullable = false, length = 150)
    private String deliveryKey;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private OffsetDateTime nextAttemptAt;

    public Notification(
            User user,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            UUID eventId,
            String deliveryKey,
            String referenceType,
            String referenceId
    ) {
        this.user = user;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.eventId = eventId;
        this.deliveryKey = deliveryKey;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.status = NotificationStatus.PENDING;
        this.attemptCount = 0;
    }

    public void markSent(OffsetDateTime sentAt) {
        this.status = NotificationStatus.SENT;
        this.sentAt = sentAt;
        this.nextAttemptAt = null;
    }

    public void markDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.nextAttemptAt = null;
    }

    public void markRead(OffsetDateTime readAt) {
        if (this.readAt == null) {
            this.readAt = readAt;
        }
    }

    public void markProcessing() {
        this.status = NotificationStatus.PROCESSING;
        this.attemptCount++;
    }

    public void markFailed(OffsetDateTime nextAttemptAt) {
        this.status = NotificationStatus.FAILED;
        this.nextAttemptAt = nextAttemptAt;
    }

    public void cancel() {
        this.status = NotificationStatus.CANCELLED;
        this.nextAttemptAt = null;
    }
}