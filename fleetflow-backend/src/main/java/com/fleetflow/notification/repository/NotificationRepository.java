package com.fleetflow.notification.repository;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUser_IdOrderByCreatedAtDesc(
            UUID userId,
            Pageable pageable
    );

    Optional<Notification> findByIdAndUser_Id(
            UUID notificationId,
            UUID userId
    );

    long countByUser_IdAndReadAtIsNull(UUID userId);

    boolean existsByDeliveryKey(String deliveryKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
           set n.readAt = :readAt
         where n.user.id = :userId
           and n.readAt is null
    """)
    int markAllRead(
            @Param("userId") UUID userId,
            @Param("readAt") OffsetDateTime readAt
    );

    @Query("""
        select n
          from Notification n
         where n.status = :status
           and (n.nextAttemptAt is null or n.nextAttemptAt <= :now)
         order by n.createdAt asc
    """)
    Page<Notification> findDueNotifications(
            @Param("status") NotificationStatus status,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}