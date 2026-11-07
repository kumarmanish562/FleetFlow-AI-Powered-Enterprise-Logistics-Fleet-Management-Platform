
package com.fleetflow.notification.repository;

import com.fleetflow.notification.entity.ProcessedNotificationEvent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedNotificationEventRepository
        extends JpaRepository<ProcessedNotificationEvent, UUID> {

    boolean existsByEventId(UUID eventId);
}
