package com.edgar.taskflow.auth.controller;

import com.edgar.taskflow.auth.dto.AuthRequest;
import com.edgar.taskflow.auth.dto.LoginRequest;
import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.auth.service.AuthService;
import com.edgar.taskflow.auth.service.SecurityEventService;
import com.edgar.taskflow.auth.session.SessionService;
import com.edgar.taskflow.auth.token.RefreshTokenService;
import com.edgar.taskflow.auth.token.TokenReuseDetectionService;
import com.edgar.taskflow.auth.token.TokenRotationService;
import com.edgar.taskflow.auth.token.risk.RiskAnalysisService;
import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.BlacklistedTokenRepository;
import com.edgar.taskflow.security.DeviceFingerprintService;
import com.edgar.taskflow.security.JwtService;
import com.edgar.taskflow.security.LoginAlertService;
import com.edgar.taskflow.security.LoginAttemptService;
import com.edgar.taskflow.security.device.DeviceDetectorService;
import com.edgar.taskflow.security.risk.ImpossibleTravelService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	    controllers = AuthController.class,
	    excludeAutoConfiguration = {
	        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
	    }
	)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
    private PasswordEncoder passwordEncoder;
	
    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
     
    @MockBean
    private User user;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private ActiveSessionResponse activeSessionResponse;
    
    @MockBean
    private AuthRequest authRequest;
    
    @MockBean
    private LoginRequest loginRequest;
    
    @MockBean
    private AuthService authService;
    
    @MockBean
    private Role role;
    
    @MockBean
    private RefreshTokenService refreshTokenService;
   
    @MockBean
    private TokenRotationService tokenRotationService;
    
    @MockBean
    private TokenReuseDetectionService reuseDetectionService;
    
    @MockBean
    private SessionService sessionService;
    
    @MockBean
    private RiskAnalysisService riskAnalysisService;
    
    @MockBean
    private SecurityEventService securityEventService;
    
    @MockBean
    private BlacklistedTokenRepository blacklistedTokenRepository;
    
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    
    @MockBean
    private JwtService jwtService;
    
    @MockBean
    private LoginAttemptService loginAttemptService;
    
    @MockBean
    private LoginAlertService loginAlertService;
    
    @MockBean
    private DeviceFingerprintService deviceFingerprintService;
    
    @MockBean
    private DeviceDetectorService deviceDetectorService;
    
    @MockBean
    private ImpossibleTravelService impossibleTravelService;

    @MockBean
    private HttpServletRequest request;
    
    @MockBean
    private HttpServletResponse response;

    // =========================
    // REGISTER TEST
    // =========================

    @Test
    void shouldRegisterUser() throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("edgar");
        request.setPassword("123456");

        Mockito.when(passwordEncoder.encode(any()))
                .thenReturn("encodedPassword");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    // =========================
    // LOGIN TEST
    // =========================

    @Test
    void shouldLoginSuccessfully() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setUsername("edgar");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(authService)
                .login(any(LoginRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    // =========================
    // REFRESH TEST
    // =========================

    @Test
    void shouldRefreshToken() throws Exception {

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token refreshed"));

        Mockito.verify(authService)
                .refresh(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    // =========================
    // LOGOUT TEST
    // =========================

    @Test
    void shouldLogoutSuccessfully() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));

        Mockito.verify(authService)
                .logoutCurrentSession(any(HttpServletRequest.class));
    }

    // =========================
    // GET SESSIONS TEST
    // =========================

    @Test
    void shouldReturnActiveSessions() throws Exception {

        ActiveSessionResponse session = ActiveSessionResponse.builder()
                .familyId("family1")
                .sessionStart(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .current(true)
                .ipAddress("127.0.0.1")
                .userAgent("Chrome")
                .deviceName("Laptop")
                .browser("Chrome")
                .location("Colombia")
                .lastActivity(LocalDateTime.now())
                .build();

        Mockito.when(authService.getSessions(any()))
                .thenReturn(List.of(session));

        mockMvc.perform(get("/api/auth/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].familyId").value("family1"));

        Mockito.verify(authService)
                .getSessions(any(HttpServletRequest.class));
    }


    // =========================
    // REVOKE SESSION TEST
    // =========================

    @Test
    void shouldRevokeSession() throws Exception {

        mockMvc.perform(delete("/api/auth/sessions/{familyId}", "family123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session revoked successfully"));

        Mockito.verify(authService)
                .revokeSession(eq("family123"));
    }
}
