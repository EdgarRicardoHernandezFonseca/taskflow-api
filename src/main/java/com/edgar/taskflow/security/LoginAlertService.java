package com.edgar.taskflow.security;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAlertService {
	
	private final RefreshTokenRepository refreshTokenRepository;

    public void checkSuspiciousLogin(User user, String ip, String userAgent) {

        Optional<RefreshToken> lastSession =
                refreshTokenRepository
                        .findTopByUserOrderBySessionStartDesc(user);

        if (lastSession.isEmpty()) {
            return;
        }

        RefreshToken previous = lastSession.get();

        boolean ipChanged =
                previous.getIpAddress() != null &&
                !previous.getIpAddress().equals(ip);

        boolean deviceChanged =
                previous.getUserAgent() != null &&
                !previous.getUserAgent().equals(userAgent);

        if (ipChanged || deviceChanged) {

            log.warn("""
                    ⚠️ Suspicious login detected

                    User: {}
                    Previous IP: {}
                    New IP: {}

                    Previous Device: {}
                    New Device: {}
                    """,
                    user.getUsername(),
                    previous.getIpAddress(),
                    ip,
                    previous.getUserAgent(),
                    userAgent
            );
        }
    }
}
