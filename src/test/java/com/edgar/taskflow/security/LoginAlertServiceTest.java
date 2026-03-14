package com.edgar.taskflow.security;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAlertServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LoginAlertService loginAlertService;

    @Test
    void shouldReturnWhenNoPreviousSession() {

        User user = new User();
        user.setUsername("edgar");

        when(refreshTokenRepository
                .findTopByUserOrderBySessionStartDesc(user))
                .thenReturn(Optional.empty());

        loginAlertService.checkSuspiciousLogin(
                user,
                "127.0.0.1",
                "Chrome"
        );

        verify(refreshTokenRepository)
                .findTopByUserOrderBySessionStartDesc(user);
    }

    @Test
    void shouldDetectSuspiciousLogin() {

        User user = new User();
        user.setUsername("edgar");

        RefreshToken token = new RefreshToken();
        token.setIpAddress("192.168.1.1");
        token.setUserAgent("Firefox");

        when(refreshTokenRepository
                .findTopByUserOrderBySessionStartDesc(user))
                .thenReturn(Optional.of(token));

        loginAlertService.checkSuspiciousLogin(
                user,
                "127.0.0.1",
                "Chrome"
        );

        verify(refreshTokenRepository)
                .findTopByUserOrderBySessionStartDesc(user);
    }
}