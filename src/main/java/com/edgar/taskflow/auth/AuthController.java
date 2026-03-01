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

    @Transactional
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {

    	 String refreshToken = request.getRefreshToken();
    	
    	 RefreshToken storedToken = refreshTokenRepository
    	            .findByToken(refreshToken)
    	            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    	    // 🔥 1. Detectar reuse
    	    if (storedToken.isUsed() || storedToken.isRevoked()) {

    	        // POSIBLE ATAQUE → invalidar toda la sesión
    	        refreshTokenRepository.revokeAllByUser(storedToken.getUser());

    	        throw new RuntimeException("Refresh token reuse detected. Session revoked.");
    	    }

    	    // 🔥 2. Verificar expiración
    	    if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
    	        storedToken.setRevoked(true);
    	        refreshTokenRepository.save(storedToken);
    	        throw new RuntimeException("Refresh token expired");
    	    }

    	    // 🔁 3. Marcar el actual como usado
    	    storedToken.setUsed(true);
    	    refreshTokenRepository.save(storedToken);

    	    // 🔁 4. Crear nuevo refresh
    	    String newRefreshToken = UUID.randomUUID().toString();

    	    RefreshToken newToken = RefreshToken.builder()
    	            .token(newRefreshToken)
    	            .user(storedToken.getUser())
    	            .expiryDate(LocalDateTime.now().plusDays(7))
    	            .revoked(false)
    	            .used(false)
    	            .build();

    	    refreshTokenRepository.save(newToken);

    	    String newAccessToken = jwtService.generateToken(storedToken.getUser());

    	    return new AuthResponse(newAccessToken, newRefreshToken);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logged out");
    }
}