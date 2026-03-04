package com.edgar.taskflow.auth;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import com.edgar.taskflow.exception.ResourceNotFoundException;
import com.edgar.taskflow.exception.InvalidTokenException;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

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
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        authService.login(request, response);
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.refresh(request, response);
        return ResponseEntity.ok("Token refreshed");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logged out");
    }
    
    @GetMapping("/sessions")
    public ResponseEntity<List<ActiveSessionResponse>> getSessions(
            HttpServletRequest request
    ) {

        String accessToken = extractAccessToken(request);

        String username = jwtService.extractUsername(accessToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Obtener familyId desde refresh cookie
        String refreshRaw = extractCookie(request, "refresh_token");
        String currentFamilyId = null;

        if (refreshRaw != null && refreshRaw.contains(".")) {
            String tokenId = refreshRaw.split("\\.")[0];
            RefreshToken token = refreshTokenRepository.findByTokenId(tokenId).orElse(null);
            if (token != null) {
                currentFamilyId = token.getFamilyId();
            }
        }

        return ResponseEntity.ok(
                authService.getActiveSessions(user, currentFamilyId)
        );
    }
    
    private String extractAccessToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("No token provided");
        }
        return authHeader.substring(7);
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