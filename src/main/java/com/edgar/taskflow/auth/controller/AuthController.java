package com.edgar.taskflow.auth.controller;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
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

    @Operation(summary = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        authService.login(request, httpRequest, response);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Refresh access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.refresh(request, response);
        return ResponseEntity.ok("Token refreshed");
    }

    @Operation(summary = "Logout current session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logoutCurrentSession(request);
        return ResponseEntity.ok("Logged out");
    }
    
    @Operation(summary = "Get active sessions for current user")
    @GetMapping("/sessions")
    public ResponseEntity<List<ActiveSessionResponse>> getSessions(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.getSessions(request));
    }
    
    @Operation(summary = "Revoke a specific session")
    @DeleteMapping("/sessions/{familyId}")
    public ResponseEntity<?> revokeSession(@PathVariable String familyId) {

        authService.revokeSession(familyId);

        return ResponseEntity.ok(
                Map.of("message", "Session revoked successfully")
        );
    }
}