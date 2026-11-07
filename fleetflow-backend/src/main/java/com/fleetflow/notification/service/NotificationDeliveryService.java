
package com.fleetflow.notification.service;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.entity.NotificationAttempt;
import com.fleetflow.notification.enums.NotificationStatus;
import com.fleetflow.notification.provider.NotificationDeliveryResult;
import com.fleetflow.notification.provider.NotificationProvider;
import com.fleetflow.notification.repository.NotificationAttemptRepository;
import com.fleetflow.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final NotificationAttemptRepository attemptRepository;
    private final List<NotificationProvider> providers;

    @Transactional
    public void deliver(java.util.UUID notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notification not found"));

        if (notification.getStatus() == NotificationStatus.SENT
                || notification.getStatus() == NotificationStatus.DELIVERED
                || notification.getStatus() == NotificationStatus.CANCELLED) {
            return;
        }

        Map<com.fleetflow.notification.enums.NotificationChannel,
                NotificationProvider> providerMap =
                new EnumMap<>(
                        com.fleetflow.notification.enums.NotificationChannel.class
                );

        providers.forEach(p -> providerMap.put(p.channel(), p));

        NotificationProvider provider =
                providerMap.get(notification.getChannel());

        if (provider == null) {
            throw new IllegalStateException(
                    "No provider for " + notification.getChannel()
            );
        }

        int attemptNumber =
                attemptRepository.countByNotification_Id(notificationId) + 1;

        NotificationAttempt attempt = attemptRepository.save(
                new NotificationAttempt(
                        notification,
                        attemptNumber,
                        provider.getClass().getSimpleName()
                )
        );

        notification.markProcessing();

        NotificationDeliveryResult result = provider.send(notification);

        if (result.successful()) {
            attempt.succeed(result.providerMessageId());
            notification.markSent(OffsetDateTime.now());
        } else {
            attempt.fail(
                    result.retryable(),
                    result.errorCode(),
                    result.errorMessage()
            );

            notification.markFailed(
                    result.retryable()
                            ? OffsetDateTime.now().plusMinutes(2)
                            : null
            );
        }
    }
}
