package com.edgar.taskflow.auth;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
}