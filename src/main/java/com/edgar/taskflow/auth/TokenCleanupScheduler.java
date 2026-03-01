package com.edgar.taskflow.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.edgar.taskflow.security.BlacklistedTokenRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

	 private final BlacklistedTokenRepository repository;

	 @Scheduled(cron = "0 0 * * * *") // cada hora
	 public void cleanExpiredTokens() {
	     repository.deleteByExpiryDateBefore(LocalDateTime.now());
	 }
}
