package com.edgar.taskflow.auth.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.auth.dto.LoginRequest;
import com.edgar.taskflow.auth.session.SessionService;
import com.edgar.taskflow.auth.token.RefreshTokenService;
import com.edgar.taskflow.auth.token.TokenReuseDetectionService;
import com.edgar.taskflow.auth.token.TokenRotationService;
import com.edgar.taskflow.auth.token.risk.RiskAnalysisService;
import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.exception.InvalidTokenException;
import com.edgar.taskflow.exception.ResourceNotFoundException;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.BlacklistedToken;
import com.edgar.taskflow.security.BlacklistedTokenRepository;
import com.edgar.taskflow.security.DeviceFingerprintService;
import com.edgar.taskflow.security.JwtService;
import com.edgar.taskflow.security.LoginAlertService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.edgar.taskflow.security.LoginAttemptService;
import com.edgar.taskflow.security.device.DeviceDetectorService;
import com.edgar.taskflow.security.device.DeviceInfo;
import com.edgar.taskflow.security.risk.ImpossibleTravelService;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final RefreshTokenService refreshTokenService;
	private final TokenRotationService tokenRotationService;
	private final TokenReuseDetectionService reuseDetectionService;
	private final SessionService sessionService;
	private final RiskAnalysisService riskAnalysisService;

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final LoginAlertService loginAlertService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final DeviceDetectorService deviceDetectorService;
    private final ImpossibleTravelService impossibleTravelService;
    
    private static final int MAX_SESSION_DAYS = 30;
    private static final int REFRESH_TOKEN_DAYS = 7;
    private static final int ACCESS_TOKEN_MINUTES = 15;
    private static final int MAX_ACTIVE_SESSIONS = 5;

    // =========================================================
    // LOGIN
    // =========================================================
    @Transactional
    public void login(LoginRequest request,
                      HttpServletRequest httpRequest,
                      HttpServletResponse response) {

        validateLoginAttempts(request.getUsername());

        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        
        String currentLocation = ip;
        
        Authentication authentication = authenticateUser(request);
        
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        riskAnalysisService.analyzeLogin(
                user,
                ip,
                currentLocation,
                deviceDetectorService.detect(userAgent)
        );
        
        
        loginAlertService.checkSuspiciousLogin(user, ip, userAgent);

        loginAttemptService.loginSucceeded(username);

        enforceSessionLimit(user);

        DeviceInfo deviceInfo = deviceDetectorService.detect(userAgent);
        
        String fingerprint = deviceFingerprintService.generateFingerprint(httpRequest);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        user,
                        deviceInfo,
                        ip,
                        userAgent,
                        fingerprint
                );

        String accessToken = jwtService.generateToken(user);

        addAccessCookie(response, accessToken);

        addRefreshCookie(
                response,
                refreshToken.getTokenId(),
                refreshToken.getRawSecret(),
                REFRESH_TOKEN_DAYS * 24 * 60 * 60
        );
    }

    // =========================================================
    // REFRESH (Sliding Expiration Real + Rotation + Reuse Detection)
    // =========================================================
    @Transactional
    public void refresh(HttpServletRequest request, HttpServletResponse response) {

        String rawRefreshToken = extractCookie(request, "refresh_token");

        if (rawRefreshToken == null) {
            throw new InvalidTokenException("No refresh token");
        }

        String[] parts = rawRefreshToken.split("\\.");

        if (parts.length != 2) {
            throw new InvalidTokenException("Invalid token format");
        }
        
        
        String tokenId = parts[0];
        String rawSecret = parts[1];

        RefreshToken storedToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        
        String currentFingerprint =
                deviceFingerprintService.generateFingerprint(request);

        if(!currentFingerprint.equals(storedToken.getDeviceFingerprint())){

            revokeTokenFamily(storedToken.getFamilyId());

            throw new InvalidTokenException(
                    "Device mismatch detected"
            );
        }

        reuseDetectionService.detectReuse(storedToken);

        if (!BCrypt.checkpw(rawSecret, storedToken.getTokenHash())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            revokeTokenFamily(storedToken.getFamilyId());
            throw new InvalidTokenException("Refresh token expired");
        }

        RefreshToken newToken = tokenRotationService.rotateToken(storedToken);
        	
        String newAccessToken = jwtService.generateToken(storedToken.getUser());

        addAccessCookie(response, newAccessToken);

        addRefreshCookie(
                response,
                newToken.getTokenId(),
                newToken.getRawSecret(),
                REFRESH_TOKEN_DAYS * 24 * 60 * 60
        );
    }

    // =========================================================
    // LOGOUT
    // =========================================================
    @Transactional
    public void logout(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("No token provided");
        }

        String accessToken = authHeader.substring(7);

        if (!blacklistedTokenRepository.existsByToken(accessToken)) {

            blacklistedTokenRepository.save(
                    BlacklistedToken.builder()
                            .token(accessToken)
                            .expiryDate(
                                    jwtService.extractExpiration(accessToken)
                                            .toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                            )
                            .build()
            );
        }

        String username = jwtService.extractUsername(accessToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
    }
    
    @Transactional
    public void logoutCurrentSession(HttpServletRequest request) {

        String refreshRaw = extractCookie(request, "refresh_token");

        if (refreshRaw == null || !refreshRaw.contains(".")) {
            return;
        }

        String tokenId = refreshRaw.split("\\.")[0];

        RefreshToken token = refreshTokenRepository
                .findByTokenId(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Token not found"));

        revokeTokenFamily(token.getFamilyId());
    }
    
    // =========================================================
    // ACTIVE SESSIONS
    // =========================================================
    @Transactional
    public List<ActiveSessionResponse> getSessions(HttpServletRequest request) {

        String accessToken = extractAccessToken(request);

        String username = jwtService.extractUsername(accessToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return sessionService.getUserSessions(user);
    }
    
    @Transactional
    public void revokeSession(String familyId) {

        sessionService.revokeSession(familyId);

    }
    
    // =========================================================
    // HELPERS
    // =========================================================

    private void addAccessCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_MINUTES * 60);
        response.addCookie(cookie);
    }

    private void addRefreshCookie(HttpServletResponse response,
                                  String tokenId,
                                  String secret,
                                  int maxAgeSeconds) {

        Cookie cookie = new Cookie("refresh_token", tokenId + "." + secret);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
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
     
    @Transactional
    private void revokeTokenFamily(String familyId) {

        List<RefreshToken> familyTokens =
                refreshTokenRepository.findByFamilyId(familyId);

        for (RefreshToken token : familyTokens) {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(familyTokens);
    }
    
    private String extractAccessToken(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("No token provided");
        }

        return authHeader.substring(7);
    }
    
    private void validateLoginAttempts(String username) {

        if (loginAttemptService.isBlocked(username)) {
            throw new RuntimeException("Too many login attempts. Try again later.");
        }

    }
    
    private Authentication authenticateUser(LoginRequest request) {

        try {

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

        } catch (Exception e) {

            loginAttemptService.loginFailed(request.getUsername());

            throw new RuntimeException("Invalid credentials");
        }
    }
    
    private void enforceSessionLimit(User user) {

        List<RefreshToken> activeSessions =
                refreshTokenRepository.findByUserAndRevokedFalse(user);

        long rootSessions = activeSessions.stream()
                .filter(t -> t.getParentToken() == null)
                .count();

        if (rootSessions >= MAX_ACTIVE_SESSIONS) {

            RefreshToken oldestSession = activeSessions.stream()
                    .filter(t -> t.getParentToken() == null)
                    .min((a, b) -> a.getSessionStart().compareTo(b.getSessionStart()))
                    .orElse(null);

            if (oldestSession != null) {
                revokeTokenFamily(oldestSession.getFamilyId());
            }
        }
    }
}