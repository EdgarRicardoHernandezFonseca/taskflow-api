package com.edgar.taskflow.auth.service;

import com.edgar.taskflow.audit.service.SecurityEventService;
import com.edgar.taskflow.auth.dto.LoginRequest;
import com.edgar.taskflow.auth.session.SessionService;
import com.edgar.taskflow.auth.token.RefreshTokenService;
import com.edgar.taskflow.auth.token.TokenReuseDetectionService;
import com.edgar.taskflow.auth.token.TokenRotationService;
import com.edgar.taskflow.auth.token.risk.RiskAnalysisService;
import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.BlacklistedTokenRepository;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.*;
import com.edgar.taskflow.security.device.DeviceDetectorService;
import com.edgar.taskflow.security.device.DeviceInfo;
import com.edgar.taskflow.security.jwt.JwtService;
import com.edgar.taskflow.security.risk.ImpossibleTravelService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
	
	@InjectMocks
    private AuthService authService;

    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenRotationService tokenRotationService;
    @Mock private TokenReuseDetectionService reuseDetectionService;
    @Mock private SessionService sessionService;
    @Mock private RiskAnalysisService riskAnalysisService;
    @Mock private SecurityEventService securityEventService;

    @Mock private BlacklistedTokenRepository blacklistedTokenRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private LoginAlertService loginAlertService;
    @Mock private DeviceFingerprintService deviceFingerprintService;
    @Mock private DeviceDetectorService deviceDetectorService;
    @Mock private ImpossibleTravelService impossibleTravelService;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void shouldLoginSuccessfully() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("edgar");
        loginRequest.setPassword("1234");

        Authentication authentication = mock(Authentication.class);

        User user = User.builder()
                .username("edgar")
                .email("edgar@email.com")
                .password("123")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId("token123")
                .rawSecret("secret")
                .build();
        
        DeviceInfo deviceInfo = new DeviceInfo("Laptop","Chrome","Windows");

        when(deviceDetectorService.detect(any()))
                .thenReturn(deviceInfo);

        when(deviceFingerprintService.generateFingerprint(any()))
                .thenReturn("fingerprint123");


        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getName()).thenReturn("edgar");

        when(userRepository.findByUsername("edgar"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("accessToken");

        when(refreshTokenService.createRefreshToken(
                any(), any(), any(), any(), any()))
                .thenReturn(refreshToken);

        authService.login(loginRequest, request, response);

        verify(securityEventService)
                .logEvent(eq("edgar"), eq("LOGIN_SUCCESS"), any(), any(), any());

        verify(response, atLeastOnce()).addCookie(any(Cookie.class));
    }

    @Test
    void shouldRefreshTokenSuccessfully(){

        Cookie cookie = new Cookie("refresh_token","id.secret");

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(deviceFingerprintService.generateFingerprint(request))
                .thenReturn("fingerprint");

        User user = User.builder()
                .username("edgar")
                .build();

        String hash = BCrypt.hashpw("secret", BCrypt.gensalt());

        RefreshToken storedToken = RefreshToken.builder()
                .tokenId("id")
                .tokenHash(hash)
                .deviceFingerprint("fingerprint")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();

        when(deviceFingerprintService.generateFingerprint(request))
        		.thenReturn("fingerprint");
        
        when(refreshTokenRepository.findByTokenId("id"))
                .thenReturn(Optional.of(storedToken));

        when(jwtService.generateToken(user))
                .thenReturn("newAccess");

        RefreshToken rotated = RefreshToken.builder()
                .tokenId("newId")
                .rawSecret("newSecret")
                .build();

        when(tokenRotationService.rotateToken(storedToken))
                .thenReturn(rotated);

        authService.refresh(request,response);

        verify(tokenRotationService).rotateToken(storedToken);
        verify(response, atLeastOnce()).addCookie(any());
    }

    @Test
    void shouldLogoutUser(){

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token123");

        when(jwtService.extractUsername("token123"))
                .thenReturn("edgar");
        
        when(jwtService.extractExpiration("token123"))
        		.thenReturn(new Date());

        User user = User.builder()
                .username("edgar")
                .build();

        when(userRepository.findByUsername("edgar"))
                .thenReturn(Optional.of(user));

        authService.logout(request);

        verify(refreshTokenRepository)
                .revokeAllByUser(user);
    }
    
    @Test
    void shouldReturnActiveSessions(){

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token123");

        when(jwtService.extractUsername("token123"))
                .thenReturn("edgar");

        User user = User.builder()
                .username("edgar")
                .build();

        when(userRepository.findByUsername("edgar"))
                .thenReturn(Optional.of(user));

        authService.getSessions(request);

        verify(sessionService).getUserSessions(user);
    }
}
