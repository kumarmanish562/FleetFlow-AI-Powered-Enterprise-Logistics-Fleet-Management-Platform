package com.fleetflow.notification.repository;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationStatus;
import com.fleetflow.notification.enums.NotificationType;
import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.user.entity.User;
import com.fleetflow.user.enums.UserStatus;
import com.fleetflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;
import java.time.OffsetDateTime;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class NotificationRepositoryTest extends PostgresContainerTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {

        notificationRepository.deleteAll();

        user = User.builder()
                .email("notification-test@fleetflow.com")
                .passwordHash("test-password-hash")
                .firstName("Notification")
                .lastName("Test")
                .phone("9876543210")
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.saveAndFlush(user);
    }

    @Test
    void shouldFindUserNotificationsNewestFirst() {

        Notification first = new Notification(user, NotificationType.SHIPMENT_CREATED,
                NotificationChannel.IN_APP, "First Notification", "First notification message",
                UUID.randomUUID(), UUID.randomUUID().toString(), null, null);
        first.markSent(OffsetDateTime.now());

        Notification second = new Notification(user, NotificationType.SHIPMENT_CREATED,
                NotificationChannel.IN_APP, "Second Notification", "Second notification message",
                UUID.randomUUID(), UUID.randomUUID().toString(), null, null);
        second.markSent(OffsetDateTime.now());

        notificationRepository.saveAndFlush(first);
        notificationRepository.saveAndFlush(second);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Notification> result =
                notificationRepository.findByUser_IdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );

        assertThat(result).hasSize(2);

        assertThat(result.getContent().get(0).getTitle())
                .isEqualTo("Second Notification");

        assertThat(result.getContent().get(1).getTitle())
                .isEqualTo("First Notification");
    }
}
