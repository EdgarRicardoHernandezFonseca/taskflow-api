package com.edgar.taskflow.auth;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.BlacklistedToken;
import com.edgar.taskflow.security.BlacklistedTokenRepository;
import com.edgar.taskflow.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public AuthResponse login(@RequestBody AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.deleteByUser(user);

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        return new AuthResponse(accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = storedToken.getUser();

        // 🔄 ROTACIÓN (muy importante)
        refreshTokenRepository.delete(storedToken);

        String newRefreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(newRefreshToken)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        String newAccessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(
                new AuthResponse(newAccessToken, newRefreshToken)
        );
    }
    
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token provided");
        }

        String token = authHeader.substring(7);

        if (!blacklistedTokenRepository.existsByToken(token)) {
            blacklistedTokenRepository.save(
                    BlacklistedToken.builder()
                            .token(token)
                            .build()
            );
        }

        String username = jwtService.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUser(user);

        return ResponseEntity.ok("Logged out successfully");
    }
}