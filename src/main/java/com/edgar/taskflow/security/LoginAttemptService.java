package com.edgar.taskflow.security;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
	
	private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_MINUTES = 15;

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockedUntil = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attempts.remove(username);
        blockedUntil.remove(username);
    }

    public void loginFailed(String username) {

        attempts.put(username, attempts.getOrDefault(username, 0) + 1);

        if (attempts.get(username) >= MAX_ATTEMPTS) {
            blockedUntil.put(username, LocalDateTime.now().plusMinutes(BLOCK_MINUTES));
        }
    }

    public boolean isBlocked(String username) {

        LocalDateTime blocked = blockedUntil.get(username);

        if (blocked == null) {
            return false;
        }

        if (blocked.isBefore(LocalDateTime.now())) {
            blockedUntil.remove(username);
            attempts.remove(username);
            return false;
        }

        return true;
    }
}
