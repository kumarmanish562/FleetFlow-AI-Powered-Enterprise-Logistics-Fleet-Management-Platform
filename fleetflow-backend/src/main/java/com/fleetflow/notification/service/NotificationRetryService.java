
package com.fleetflow.notification.service;

import com.fleetflow.notification.enums.NotificationStatus;
import com.fleetflow.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class NotificationRetryService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService deliveryService;

    @Scheduled(fixedDelayString = "${fleetflow.notifications.retry.delay-ms:30000}")
    public void retryDueNotifications() {
        var due = notificationRepository.findDueNotifications(
                NotificationStatus.FAILED,
                OffsetDateTime.now(),
                PageRequest.of(0, 100)
        );

        due.forEach(notification -> {
            try {
                deliveryService.deliver(notification.getId());
            } catch (RuntimeException exception) {
                // Record structured logs and metrics in the production
                // implementation. Do not log recipient PII or message bodies.
            }
        });
    }
}
