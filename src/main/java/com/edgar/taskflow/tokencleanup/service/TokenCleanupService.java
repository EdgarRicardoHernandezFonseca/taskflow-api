package com.edgar.taskflow.tokencleanup.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.edgar.taskflow.repository.BlacklistedTokenRepository;
import com.edgar.taskflow.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenCleanupService {
	
	private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Scheduled(cron = "0 0 * * * *") 
    // cada hora
    public void cleanupExpiredTokens() {

        LocalDateTime now = LocalDateTime.now();

        refreshTokenRepository.deleteByExpiryDateBefore(now);

        blacklistedTokenRepository.deleteByExpiryDateBefore(now);
    }

}
