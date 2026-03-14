package com.edgar.taskflow.security;

import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.entity.User;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void shouldGenerateToken() {

        User user = new User();
        user.setUsername("edgar");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsername() {

        User user = new User();
        user.setUsername("edgar");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertEquals("edgar", username);
    }

    @Test
    void shouldValidateToken() {

        User user = new User();
        user.setUsername("edgar");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, "edgar");

        assertTrue(valid);
    }

    @Test
    void shouldReturnExpirationDate() {

        User user = new User();
        user.setUsername("edgar");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
}