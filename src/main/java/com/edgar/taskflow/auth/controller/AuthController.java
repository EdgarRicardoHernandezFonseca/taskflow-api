package com.edgar.taskflow.auth.controller;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.auth.dto.AuthRequest;
import com.edgar.taskflow.auth.dto.LoginRequest;
import com.edgar.taskflow.auth.service.AuthService;
import com.edgar.taskflow.entity.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        authService.login(request, httpRequest, response);

        return ResponseEntity.ok().build();
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
        authService.logoutCurrentSession(request);
        return ResponseEntity.ok("Logged out");
    }
    
    @GetMapping("/sessions")
    public ResponseEntity<List<ActiveSessionResponse>> getSessions(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.getSessions(request));
    }
    
    @DeleteMapping("/sessions/{familyId}")
    public ResponseEntity<?> revokeSession(@PathVariable String familyId) {

        authService.revokeSession(familyId);

        return ResponseEntity.ok(
                Map.of("message", "Session revoked successfully")
        );
    }
}