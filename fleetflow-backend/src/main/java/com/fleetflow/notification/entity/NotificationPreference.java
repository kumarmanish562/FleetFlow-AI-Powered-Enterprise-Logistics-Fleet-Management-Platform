
package com.fleetflow.notification.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;
import com.fleetflow.user.entity.User;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_preference",
                columnNames = {"user_id", "notification_type", "channel"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "consent_at")
    private OffsetDateTime consentAt;

    public NotificationPreference(
            User user,
            NotificationType notificationType,
            NotificationChannel channel,
            boolean enabled,
            OffsetDateTime consentAt
    ) {
        this.user = user;
        this.notificationType = notificationType;
        this.channel = channel;
        this.enabled = enabled;
        this.consentAt = consentAt;
    }

    public void update(boolean enabled, OffsetDateTime consentAt) {
        this.enabled = enabled;
        this.consentAt = consentAt;
    }
}
