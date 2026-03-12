package com.edgar.taskflow.repository;

import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.entity.User;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByUsername() {

        User user = User.builder()
                .username("edgar")
                .email("edgar@email.com")
                .password("123456")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Optional<User> result = userRepository.findByUsername("edgar");

        assertTrue(result.isPresent());
        assertEquals("edgar@email.com", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyIfUserNotFound() {

        Optional<User> result = userRepository.findByUsername("unknown");

        assertTrue(result.isEmpty());
    }
}
