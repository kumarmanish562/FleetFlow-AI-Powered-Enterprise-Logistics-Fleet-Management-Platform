package com.fleetflow.notification.service;

import com.fleetflow.notification.dto.NotificationResponse;
import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.repository.NotificationRepository;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationResponse> getNotifications(
            UUID userId,
            Pageable pageable
    ) {
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    public NotificationResponse getNotification(
            UUID userId,
            UUID notificationId
    ) {
        return findOwnedNotification(userId, notificationId);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndReadAtIsNull(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(
            UUID userId,
            UUID notificationId
    ) {
        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Notification not found"
                        )
                );

        notification.markRead(OffsetDateTime.now());

        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllRead(
                userId,
                OffsetDateTime.now()
        );
    }

    private NotificationResponse findOwnedNotification(
            UUID userId,
            UUID notificationId
    ) {
        return notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .map(NotificationResponse::from)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Notification not found"
                        )
                );
    }
}