package com.edgar.taskflow.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        attempts.merge(username, 1, Integer::sum);
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {
        return attempts.getOrDefault(username, 0) >= 5;
    }
}
