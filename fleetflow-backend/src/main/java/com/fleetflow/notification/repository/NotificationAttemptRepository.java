
package com.fleetflow.notification.repository;

import com.fleetflow.notification.entity.NotificationAttempt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationAttemptRepository
        extends JpaRepository<NotificationAttempt, UUID> {

    List<NotificationAttempt>
    findByNotification_IdOrderByAttemptNumberDesc(UUID notificationId);

    int countByNotification_Id(UUID notificationId);
}
