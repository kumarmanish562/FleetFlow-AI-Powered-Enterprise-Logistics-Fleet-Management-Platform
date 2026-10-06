package com.fleetflow.database;

import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.user.entity.User;
import com.fleetflow.user.enums.UserStatus;
import com.fleetflow.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BaseEntityAuditTest extends PostgresContainerTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSetCreatedAndUpdatedAt() {

        User user = User.builder()
                .email("audit@example.com")
                .passwordHash("password")
                .firstName("Audit")
                .status(UserStatus.ACTIVE)
                .build();

        User saved =
                userRepository.saveAndFlush(user);

        assertThat(saved.getCreatedAt())
                .isNotNull();

        assertThat(saved.getUpdatedAt())
                .isNotNull();
    }

    @Test
    void shouldUpdateUpdatedAt() {

        User user = User.builder()
                .email("audit-update@example.com")
                .passwordHash("password")
                .firstName("Old")
                .status(UserStatus.ACTIVE)
                .build();

        User saved =
                userRepository.saveAndFlush(user);

        var originalUpdatedAt =
                saved.getUpdatedAt();

        saved.setFirstName("New");

        User updated =
                userRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt())
                .isNotNull();

        assertThat(updated.getUpdatedAt())
                .isAfterOrEqualTo(originalUpdatedAt);
    }
}