package com.edgar.taskflow.audit.service;

import com.edgar.taskflow.audit.entity.SecurityEvent;
import com.edgar.taskflow.audit.repository.SecurityEventRepository;
import com.edgar.taskflow.audit.service.SecurityEventService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityEventServiceTest {

    @Mock
    private SecurityEventRepository repository;

    @InjectMocks
    private SecurityEventService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldLogSecurityEvent() {

        service.logEvent(
                "edgar",
                "LOGIN_SUCCESS",
                "127.0.0.1",
                "Chrome",
                "Bogota"
        );

        ArgumentCaptor<SecurityEvent> captor =
                ArgumentCaptor.forClass(SecurityEvent.class);

        verify(repository, times(1)).save(captor.capture());

        SecurityEvent savedEvent = captor.getValue();

        assertEquals("edgar", savedEvent.getUsername());
        assertEquals("LOGIN_SUCCESS", savedEvent.getEventType());
        assertEquals("127.0.0.1", savedEvent.getIpAddress());
        assertEquals("Chrome", savedEvent.getDevice());
        assertEquals("Bogota", savedEvent.getLocation());

        assertNotNull(savedEvent.getTimestamp());
    }
}
