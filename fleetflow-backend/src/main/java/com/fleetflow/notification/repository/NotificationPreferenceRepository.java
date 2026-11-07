
package com.fleetflow.notification.repository;

import com.fleetflow.notification.entity.NotificationPreference;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByUser_Id(UUID userId);

    Optional<NotificationPreference>
    findByUser_IdAndNotificationTypeAndChannel(
            UUID userId,
            NotificationType notificationType,
            NotificationChannel channel
    );
}
