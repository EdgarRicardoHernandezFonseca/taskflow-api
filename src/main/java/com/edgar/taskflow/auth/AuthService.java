package com.edgar.taskflow.auth;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
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

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    private final AuthenticationManager authenticationManager;
    
    private static final int MAX_SESSION_DAYS = 30;
    
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
    }
    
    private RefreshToken findMatchingToken(String rawToken, User user) {

        return refreshTokenRepository.findByUser(user)
                .stream()
                .filter(rt -> BCrypt.checkpw(rawToken, rt.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
    }
    
    @Transactional
    public void refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String rawRefreshToken = extractCookie(request, "refresh_token");

        if (rawRefreshToken == null) {
            throw new RuntimeException("No refresh token");
        }

        String[] parts = rawRefreshToken.split("\\.");

        if (parts.length != 2) {
            throw new RuntimeException("Invalid token format");
        }

        String tokenId = parts[0];
        String rawSecret = parts[1];

        RefreshToken storedToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!BCrypt.checkpw(rawSecret, storedToken.getTokenHash())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // 🔒 Max session lifetime
        if (storedToken.getSessionStart()
                .plusDays(MAX_SESSION_DAYS)
                .isBefore(LocalDateTime.now())) {

            refreshTokenRepository.revokeByFamilyId(storedToken.getFamilyId());
            throw new RuntimeException("Max session lifetime reached");
        }

        // 🔁 Reuse detection
        if (storedToken.isUsed() || storedToken.isRevoked()) {
            refreshTokenRepository.revokeByFamilyId(storedToken.getFamilyId());
            throw new ReuseTokenException("Refresh token reuse detected");
        }

        storedToken.setUsed(true);
        refreshTokenRepository.save(storedToken);

        // 🔄 Crear nuevo refresh (rotation)
        String newTokenId = UUID.randomUUID().toString();
        String newSecret = UUID.randomUUID().toString();
        String newHashed = BCrypt.hashpw(newSecret, BCrypt.gensalt());

        RefreshToken newToken = RefreshToken.builder()
                .tokenId(newTokenId)
                .tokenHash(newHashed)
                .familyId(storedToken.getFamilyId())
                .parentToken(storedToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .sessionStart(storedToken.getSessionStart())
                .revoked(false)
                .used(false)
                .user(storedToken.getUser())
                .build();

        refreshTokenRepository.save(newToken);
        storedToken.setReplacedByToken(newToken);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtService.generateToken(storedToken.getUser());

        // 🍪 Cookies
        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60);
        response.addCookie(accessCookie);

        String newRawRefresh = newTokenId + "." + newSecret;

        Cookie refreshCookie = new Cookie("refresh_token", newRawRefresh);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);
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
    
    @Transactional
    public void login(LoginRequest request, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Obtener username desde security
        String username = authentication.getName();

        // Buscar tu entidad real
        com.edgar.taskflow.entity.User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🔐 Generar Access Token
        String accessToken = jwtService.generateToken(user);

        // 🔁 Crear Token Family
        String familyId = UUID.randomUUID().toString();

        // 🔄 Crear Refresh Token (tokenId.secret)
        String tokenId = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();
        String hashedSecret = BCrypt.hashpw(secret, BCrypt.gensalt());

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .tokenHash(hashedSecret)
                .familyId(familyId)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .sessionStart(LocalDateTime.now())
                .revoked(false)
                .used(false)
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        // 🍪 ACCESS COOKIE
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60);
        response.addCookie(accessCookie);

        // 🍪 REFRESH COOKIE
        String rawRefresh = tokenId + "." + secret;

        Cookie refreshCookie = new Cookie("refresh_token", rawRefresh);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);
    }
}
