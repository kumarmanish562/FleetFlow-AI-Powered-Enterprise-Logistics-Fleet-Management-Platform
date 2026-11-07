
package com.fleetflow.notification.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notification_templates",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_template",
                columnNames = {
                        "notification_type", "channel", "locale"
                }
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationTemplate extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Column(nullable = false, length = 20)
    private String locale;

    @Column(name = "title_template", nullable = false, length = 255)
    private String titleTemplate;

    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    @Column(nullable = false)
    private boolean enabled = true;

    public NotificationTemplate(
            NotificationType notificationType,
            NotificationChannel channel,
            String locale,
            String titleTemplate,
            String messageTemplate
    ) {
        this.notificationType = notificationType;
        this.channel = channel;
        this.locale = locale;
        this.titleTemplate = titleTemplate;
        this.messageTemplate = messageTemplate;
        this.enabled = true;
    }
}
