
package com.fleetflow.notification.repository;

import com.fleetflow.notification.entity.NotificationTemplate;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository
        extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate>
    findFirstByNotificationTypeAndChannelAndLocaleAndEnabledTrue(
            NotificationType notificationType,
            NotificationChannel channel,
            String locale
    );
}
