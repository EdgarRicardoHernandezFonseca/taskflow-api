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

        // 🔐 Crear refresh token raw
        String rawRefreshToken = UUID.randomUUID().toString();

        // 🔐 Hashear
        String hashedToken = BCrypt.hashpw(rawRefreshToken, BCrypt.gensalt());

        // 🔐 Crear familyId
        String familyId = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashedToken)
                .familyId(familyId)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .used(false)
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken);
    }

    @Transactional
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {

        String rawRefreshToken = request.getRefreshToken();

        // 🔍 Buscar usuario desde JWT (si fuera JWT)
        // Como aquí es UUID plano, debes buscar por usuario autenticado
        // En este ejemplo buscamos por todos y comparamos

        RefreshToken storedToken = refreshTokenRepository.findAll()
                .stream()
                .filter(rt -> BCrypt.checkpw(rawRefreshToken, rt.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.isUsed() || storedToken.isRevoked()) {

            // 🚨 REUSE DETECTED → revocar familia completa
            refreshTokenRepository.revokeByFamilyId(storedToken.getFamilyId());

            throw new RuntimeException("Refresh token reuse detected");
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        // 🔁 Marcar como usado
        storedToken.setUsed(true);
        refreshTokenRepository.save(storedToken);

        // 🔁 Crear nuevo token
        String newRawToken = UUID.randomUUID().toString();
        String newHashed = BCrypt.hashpw(newRawToken, BCrypt.gensalt());

        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(newHashed)
                .familyId(storedToken.getFamilyId())
                .parentToken(storedToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .used(false)
                .user(storedToken.getUser())
                .build();

        refreshTokenRepository.save(newToken);

        storedToken.setReplacedByToken(newToken);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtService.generateToken(storedToken.getUser());

        return new AuthResponse(newAccessToken, newRawToken);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logged out");
    }
}