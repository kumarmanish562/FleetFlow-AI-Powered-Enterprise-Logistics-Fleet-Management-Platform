
package com.fleetflow.notification.service;


import com.fleetflow.notification.dto.NotificationPageResponse;
import com.fleetflow.notification.dto.NotificationResponse;
import com.fleetflow.notification.exception.NotificationNotFoundException;
import com.fleetflow.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository repository;

    public NotificationPageResponse list(
            UUID userId,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination");
        }

        var result = repository
                .findByUser_IdOrderByCreatedAtDesc(
                        userId,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                )
                .map(NotificationResponse::from);

        return NotificationPageResponse.from(result);
    }

    public NotificationResponse get(UUID userId, UUID notificationId) {
        return repository.findByIdAndUser_Id(notificationId, userId)
                .map(NotificationResponse::from)
                .orElseThrow(NotificationNotFoundException::new);
    }

    public long unreadCount(UUID userId) {
        return repository.countByUser_IdAndReadAtIsNull(userId);
    }
}
