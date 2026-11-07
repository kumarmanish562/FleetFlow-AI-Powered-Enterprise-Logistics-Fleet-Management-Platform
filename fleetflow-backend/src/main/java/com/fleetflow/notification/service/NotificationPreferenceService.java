
package com.fleetflow.notification.service;


import com.fleetflow.notification.dto.NotificationPreferenceRequest;
import com.fleetflow.notification.dto.NotificationPreferenceResponse;
import com.fleetflow.notification.entity.NotificationPreference;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;
import com.fleetflow.notification.repository.NotificationPreferenceRepository;
import com.fleetflow.user.entity.User;
import com.fleetflow.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public List<NotificationPreferenceResponse> getPreferences(UUID userId) {
        return preferenceRepository.findByUser_Id(userId)
                .stream()
                .map(NotificationPreferenceResponse::from)
                .toList();
    }

    public boolean isEnabled(
            UUID userId,
            NotificationType type,
            NotificationChannel channel
    ) {
        return preferenceRepository
                .findByUser_IdAndNotificationTypeAndChannel(
                        userId, type, channel
                )
                .map(NotificationPreference::isEnabled)
                .orElse(channel == NotificationChannel.IN_APP);
    }

    @Transactional
    public NotificationPreferenceResponse update(
            UUID userId,
            NotificationPreferenceRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        NotificationPreference preference =
                preferenceRepository
                        .findByUser_IdAndNotificationTypeAndChannel(
                                userId,
                                request.notificationType(),
                                request.channel()
                        )
                        .orElseGet(() -> new NotificationPreference(
                                user,
                                request.notificationType(),
                                request.channel(),
                                request.enabled(),
                                request.enabled()
                                        ? OffsetDateTime.now()
                                        : null
                        ));

        if (preferenceRepository.existsById(preference.getId())) {
            preference.update(
                    request.enabled(),
                    request.enabled() ? OffsetDateTime.now() : null
            );
        }

        return NotificationPreferenceResponse.from(
                preferenceRepository.save(preference)
        );
    }
}
