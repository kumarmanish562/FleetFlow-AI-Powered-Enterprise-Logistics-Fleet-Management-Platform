
package com.fleetflow.notification.service;


import com.fleetflow.notification.dto.NotificationResponse;
import com.fleetflow.notification.exception.NotificationNotFoundException;
import com.fleetflow.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationReadService {

    private final NotificationRepository repository;

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        var notification = repository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(NotificationNotFoundException::new);

        notification.markRead(OffsetDateTime.now());

        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead(UUID userId) {
        return repository.markAllRead(userId, OffsetDateTime.now());
    }
}
