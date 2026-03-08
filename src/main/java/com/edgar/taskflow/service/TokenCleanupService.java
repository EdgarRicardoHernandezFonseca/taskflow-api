package com.edgar.taskflow.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;

import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.security.BlacklistedTokenRepository;

public class TokenCleanupService {
	
	private final RefreshTokenRepository refreshTokenRepository = null;
    private final BlacklistedTokenRepository blacklistedTokenRepository = null;

    @Scheduled(cron = "0 0 * * * *") 
    // cada hora
    public void cleanupExpiredTokens() {

        LocalDateTime now = LocalDateTime.now();

        refreshTokenRepository.deleteByExpiryDateBefore(now);

        blacklistedTokenRepository.deleteByExpiryDateBefore(now);
    }

}
