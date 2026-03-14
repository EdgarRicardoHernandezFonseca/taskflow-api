package com.edgar.taskflow.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private final LoginAttemptService service =
            new LoginAttemptService();

    @Test
    void shouldNotBlockUserInitially() {

        boolean blocked = service.isBlocked("edgar");

        assertFalse(blocked);
    }

    @Test
    void shouldBlockUserAfterMaxAttempts() {

        String username = "edgar";

        for (int i = 0; i < 5; i++) {
            service.loginFailed(username);
        }

        assertTrue(service.isBlocked(username));
    }

    @Test
    void shouldResetAttemptsAfterSuccess() {

        String username = "edgar";

        for (int i = 0; i < 5; i++) {
            service.loginFailed(username);
        }

        service.loginSucceeded(username);

        assertFalse(service.isBlocked(username));
    }
}