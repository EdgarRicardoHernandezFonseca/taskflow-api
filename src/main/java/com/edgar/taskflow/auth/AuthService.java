package com.edgar.taskflow.auth;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.BlacklistedToken;
import com.edgar.taskflow.security.BlacklistedTokenRepository;
import com.edgar.taskflow.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public void logout(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No token provided");
        }

        String accessToken = authHeader.substring(7);

        // 1️⃣ Blacklist access token
        if (!blacklistedTokenRepository.existsByToken(accessToken)) {

            blacklistedTokenRepository.save(
                    BlacklistedToken.builder()
                            .token(accessToken)
                            .expiryDate(
                                    jwtService.extractExpiration(accessToken)
                                            .toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                            )
                            .build()
            );
        }

        // 2️⃣ Revocar todos los refresh tokens del usuario
        String username = jwtService.extractUsername(accessToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
    }
    
    private RefreshToken findMatchingToken(String rawToken, User user) {

        return refreshTokenRepository.findByUser(user)
                .stream()
                .filter(rt -> BCrypt.checkpw(rawToken, rt.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }
}
