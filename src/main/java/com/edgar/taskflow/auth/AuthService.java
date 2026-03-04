package com.edgar.taskflow.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.exception.InvalidTokenException;
import com.edgar.taskflow.exception.ResourceNotFoundException;
import com.edgar.taskflow.exception.ReuseTokenException;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.BlacklistedToken;
import com.edgar.taskflow.security.BlacklistedTokenRepository;
import com.edgar.taskflow.security.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final int MAX_SESSION_DAYS = 30;
    private static final int REFRESH_TOKEN_DAYS = 7;
    private static final int ACCESS_TOKEN_MINUTES = 15;

    // =========================================================
    // LOGIN
    // =========================================================
    @Transactional
    public void login(LoginRequest request, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(user);

        String familyId = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();
        String hashedSecret = BCrypt.hashpw(secret, BCrypt.gensalt());

        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .tokenHash(hashedSecret)
                .familyId(familyId)
                .expiryDate(now.plusDays(REFRESH_TOKEN_DAYS))
                .sessionStart(now)
                .revoked(false)
                .used(false)
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        addAccessCookie(response, accessToken);
        addRefreshCookie(response, tokenId, secret, REFRESH_TOKEN_DAYS * 24 * 60 * 60);
    }

    // =========================================================
    // REFRESH (Sliding Expiration Real + Rotation + Reuse Detection)
    // =========================================================
    @Transactional
    public void refresh(HttpServletRequest request, HttpServletResponse response) {

        String rawRefreshToken = extractCookie(request, "refresh_token");

        if (rawRefreshToken == null) {
            throw new InvalidTokenException("No refresh token");
        }

        String[] parts = rawRefreshToken.split("\\.");
        if (parts.length != 2) {
            throw new InvalidTokenException("Invalid token format");
        }

        String tokenId = parts[0];
        String rawSecret = parts[1];

        RefreshToken storedToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        LocalDateTime now = LocalDateTime.now();

        // 1️⃣ REUSE DETECTION (CRÍTICO)
        if (storedToken.isUsed() || storedToken.getReplacedByToken() != null) {
            revokeTokenFamily(storedToken.getFamilyId());
            throw new ReuseTokenException("Refresh token reuse detected. Session revoked.");
        }

        // 2️⃣ SECRET CHECK
        if (!BCrypt.checkpw(rawSecret, storedToken.getTokenHash())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // 3️⃣ REVOKED
        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }

        // 4️⃣ EXPIRED
        if (storedToken.getExpiryDate().isBefore(now)) {
            revokeTokenFamily(storedToken.getFamilyId());
            throw new InvalidTokenException("Refresh token expired");
        }

        // 5️⃣ MAX SESSION LIFETIME
        if (storedToken.getSessionStart()
                .plusDays(MAX_SESSION_DAYS)
                .isBefore(now)) {

            revokeTokenFamily(storedToken.getFamilyId());
            throw new InvalidTokenException("Session expired. Please login again.");
        }

        // =====================================================
        // ROTATION
        // =====================================================

        storedToken.setUsed(true);

        String newTokenId = UUID.randomUUID().toString();
        String newSecret = UUID.randomUUID().toString();
        String newHashed = BCrypt.hashpw(newSecret, BCrypt.gensalt());

        LocalDateTime newExpiry = now.plusDays(REFRESH_TOKEN_DAYS);

        LocalDateTime maxSessionExpiry =
                storedToken.getSessionStart().plusDays(MAX_SESSION_DAYS);

        if (newExpiry.isAfter(maxSessionExpiry)) {
            newExpiry = maxSessionExpiry;
        }

        RefreshToken newToken = RefreshToken.builder()
                .tokenId(newTokenId)
                .tokenHash(newHashed)
                .familyId(storedToken.getFamilyId())
                .parentToken(storedToken)
                .expiryDate(newExpiry)
                .sessionStart(storedToken.getSessionStart())
                .revoked(false)
                .used(false)
                .user(storedToken.getUser())
                .build();

        refreshTokenRepository.save(newToken);

        storedToken.setReplacedByToken(newToken);
        refreshTokenRepository.save(storedToken);

        // =====================================================
        // NEW ACCESS TOKEN
        // =====================================================

        String newAccessToken = jwtService.generateToken(storedToken.getUser());

        long secondsUntilExpiry =
                Duration.between(now, newExpiry).getSeconds();

        addAccessCookie(response, newAccessToken);
        addRefreshCookie(response, newTokenId, newSecret, (int) secondsUntilExpiry);
    }

    // =========================================================
    // LOGOUT
    // =========================================================
    @Transactional
    public void logout(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("No token provided");
        }

        String accessToken = authHeader.substring(7);

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

        String username = jwtService.extractUsername(accessToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void addAccessCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_MINUTES * 60);
        response.addCookie(cookie);
    }

    private void addRefreshCookie(HttpServletResponse response,
                                  String tokenId,
                                  String secret,
                                  int maxAgeSeconds) {

        Cookie cookie = new Cookie("refresh_token", tokenId + "." + secret);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private String extractCookie(HttpServletRequest request, String name) {

        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void revokeTokenFamily(String familyId) {

        List<RefreshToken> familyTokens =
                refreshTokenRepository.findByFamilyId(familyId);

        for (RefreshToken token : familyTokens) {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(familyTokens);
    }
}