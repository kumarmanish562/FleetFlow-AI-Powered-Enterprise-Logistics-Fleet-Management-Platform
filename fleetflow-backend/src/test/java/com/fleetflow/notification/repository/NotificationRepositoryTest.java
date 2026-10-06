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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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

        Notification first =
                Notification.builder()
                        .user(user)
                        .type(NotificationType.SHIPMENT_CREATED)
                        .channel(NotificationChannel.IN_APP)
                        .title("First Notification")
                        .message("First notification message")
                        .status(NotificationStatus.SENT)
                        .build();

        Notification second =
                Notification.builder()
                        .user(user)
                        .type(NotificationType.SHIPMENT_CREATED)
                        .channel(NotificationChannel.IN_APP)
                        .title("Second Notification")
                        .message("Second notification message")
                        .status(NotificationStatus.SENT)
                        .build();

        notificationRepository.saveAndFlush(first);
        notificationRepository.saveAndFlush(second);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Notification> result =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(
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