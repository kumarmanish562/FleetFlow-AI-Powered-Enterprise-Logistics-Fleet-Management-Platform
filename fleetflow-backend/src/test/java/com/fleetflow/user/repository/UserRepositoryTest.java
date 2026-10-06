package com.fleetflow.user.repository;

import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.user.entity.User;
import com.fleetflow.user.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserRepositoryTest extends PostgresContainerTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {

        User user = User.builder()
                .email("manish@example.com")
                .passwordHash("hashed-password")
                .firstName("Manish")
                .lastName("Kumar")
                .status(UserStatus.ACTIVE)
                .build();

        User saved =
                userRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail())
                .isEqualTo("manish@example.com");
        assertThat(saved.getCreatedAt())
                .isNotNull();
        assertThat(saved.getUpdatedAt())
                .isNotNull();
    }
}