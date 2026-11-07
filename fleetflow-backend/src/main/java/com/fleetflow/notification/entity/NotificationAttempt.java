
package com.fleetflow.notification.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.notification.enums.NotificationAttemptStatus;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "notification_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_attempt_number",
                columnNames = {"notification_id", "attempt_number"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(nullable = false, length = 50)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationAttemptStatus status;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attempted_at", nullable = false)
    private OffsetDateTime attemptedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public NotificationAttempt(
            Notification notification,
            int attemptNumber,
            String provider
    ) {
        this.notification = notification;
        this.attemptNumber = attemptNumber;
        this.provider = provider;
        this.status = NotificationAttemptStatus.STARTED;
        this.attemptedAt = OffsetDateTime.now();
    }

    public void succeed(String providerMessageId) {
        this.status = NotificationAttemptStatus.SUCCEEDED;
        this.providerMessageId = providerMessageId;
        this.completedAt = OffsetDateTime.now();
    }

    public void fail(
            boolean retryable,
            String errorCode,
            String errorMessage
    ) {
        this.status = retryable
                ? NotificationAttemptStatus.RETRYABLE_FAILURE
                : NotificationAttemptStatus.PERMANENT_FAILURE;

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.completedAt = OffsetDateTime.now();
    }
}
