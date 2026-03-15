package com.edgar.taskflow.tokencleanup.service;

import com.edgar.taskflow.repository.BlacklistedTokenRepository;
import com.edgar.taskflow.repository.RefreshTokenRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private TokenCleanupService service;

    @Test
    void shouldCleanupExpiredTokens() {

        service.cleanupExpiredTokens();

        verify(refreshTokenRepository)
                .deleteByExpiryDateBefore(any());

        verify(blacklistedTokenRepository)
                .deleteByExpiryDateBefore(any());
    }
}