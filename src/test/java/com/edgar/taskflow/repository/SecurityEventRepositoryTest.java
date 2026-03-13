package com.edgar.taskflow.repository;

import com.edgar.taskflow.entity.SecurityEvent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SecurityEventRepositoryTest {

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Test
    void shouldSaveSecurityEvent() {

        SecurityEvent event = SecurityEvent.builder()
                .username("edgar")
                .eventType("LOGIN_SUCCESS")
                .ipAddress("127.0.0.1")
                .device("Chrome")
                .location("Bogota")
                .timestamp(LocalDateTime.now())
                .build();

        SecurityEvent saved = securityEventRepository.save(event);

        assertNotNull(saved.getId());
        assertEquals("edgar", saved.getUsername());
    }

    @Test
    void shouldFindSecurityEventById() {

        SecurityEvent event = SecurityEvent.builder()
                .username("edgar")
                .eventType("LOGIN_FAILED")
                .ipAddress("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        SecurityEvent saved = securityEventRepository.save(event);

        var result = securityEventRepository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("LOGIN_FAILED", result.get().getEventType());
    }
}
