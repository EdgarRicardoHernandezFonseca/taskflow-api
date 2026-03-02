package com.edgar.taskflow.auth;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.BlacklistedToken;
import com.edgar.taskflow.security.BlacklistedTokenRepository;
import com.edgar.taskflow.security.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.edgar.taskflow.auth.RefreshTokenRepository;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;
    private static final int MAX_SESSION_DAYS = 30;
    
    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getUsername() + "@mail.com")
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(
            @RequestBody AuthRequest request,
            HttpServletResponse response
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        String accessToken = jwtService.generateToken(user);
        String tokenId = UUID.randomUUID().toString();
        String rawSecret = UUID.randomUUID().toString();

        String hashedToken = BCrypt.hashpw(rawSecret, BCrypt.gensalt());

        String rawRefreshToken = tokenId + "." + rawSecret;

        String familyId = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .tokenHash(hashedToken)
                .familyId(familyId)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .sessionStart(LocalDateTime.now())
                .revoked(false)
                .used(false)
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        // 🔐 Access cookie
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // true en producción HTTPS
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60); // 15 min
        response.addCookie(accessCookie);

        // 🔐 Refresh cookie
        Cookie refreshCookie = new Cookie("refresh_token", rawRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String rawRefreshToken = extractCookie(request, "refresh_token");

        if (rawRefreshToken == null) {
            return ResponseEntity.status(401).body("No refresh token");
        }

        String[] parts = rawRefreshToken.split("\\.");

        if (parts.length != 2) {
            throw new RuntimeException("Invalid token format");
        }

        String tokenId = parts[0];
        String rawSecret = parts[1];

        RefreshToken storedToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!BCrypt.checkpw(rawSecret, storedToken.getTokenHash())) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 🔒 Verificar límite absoluto de sesión (AHORA SÍ)
        if (storedToken.getSessionStart()
                .plusDays(MAX_SESSION_DAYS)
                .isBefore(LocalDateTime.now())) {

            refreshTokenRepository.revokeByFamilyId(storedToken.getFamilyId());
            throw new RuntimeException("Max session lifetime reached");
        }

        if (storedToken.isUsed() || storedToken.isRevoked()) {
            refreshTokenRepository.revokeByFamilyId(storedToken.getFamilyId());
            throw new RuntimeException("Reuse detected");
        }

        storedToken.setUsed(true);
        refreshTokenRepository.save(storedToken);

        String newRawToken = UUID.randomUUID().toString();
        String newHashed = BCrypt.hashpw(newRawToken, BCrypt.gensalt());

        RefreshToken newToken = RefreshToken.builder()
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

        String newAccessToken = jwtService.generateToken(storedToken.getUser());

        Cookie newAccessCookie = new Cookie("access_token", newAccessToken);
        newAccessCookie.setHttpOnly(true);
        newAccessCookie.setPath("/");
        newAccessCookie.setMaxAge(15 * 60);
        response.addCookie(newAccessCookie);

        Cookie newRefreshCookie = new Cookie("refresh_token", newRawToken);
        newRefreshCookie.setHttpOnly(true);
        newRefreshCookie.setPath("/api/auth/refresh");
        newRefreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(newRefreshCookie);

        return ResponseEntity.ok("Token refreshed");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logged out");
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
}