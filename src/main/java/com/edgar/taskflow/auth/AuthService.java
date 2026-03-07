package com.edgar.taskflow.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.auth.dto.DeviceInfo;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.exception.InvalidTokenException;
import com.edgar.taskflow.exception.ResourceNotFoundException;
import com.edgar.taskflow.exception.ReuseTokenException;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final LoginAlertService loginAlertService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final DeviceDetectorService deviceDetectorService;
    
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

        Authentication authentication = authenticateUser(request);

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        loginAlertService.checkSuspiciousLogin(user, ip, userAgent);

        loginAttemptService.loginSucceeded(username);

        enforceSessionLimit(user);

        RefreshToken refreshToken = createRefreshToken(user, ip, userAgent);

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

        LocalDateTime now = LocalDateTime.now();
         
        // 1️⃣ REUSE DETECTION (CRÍTICO)
        if (storedToken.isUsed() || storedToken.getReplacedByToken() != null) {
            revokeTokenFamily(storedToken.getFamilyId());
            throw new ReuseTokenException("Refresh token reuse detected. Session revoked.");
        }

        // 2️⃣ SECRET CHECK
        if (!BCrypt.checkpw(rawSecret, storedToken.getTokenHash())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // 3️⃣ REVOKED
        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }

        // 4️⃣ EXPIRED
        if (storedToken.getExpiryDate().isBefore(now)) {
            revokeTokenFamily(storedToken.getFamilyId());
            throw new InvalidTokenException("Refresh token expired");
        }

        // 5️⃣ MAX SESSION LIFETIME
        if (storedToken.getSessionStart()
                .plusDays(MAX_SESSION_DAYS)
                .isBefore(now)) {

            revokeTokenFamily(storedToken.getFamilyId());
            throw new InvalidTokenException("Session expired. Please login again.");
        }

        // =====================================================
        // ROTATION
        // =====================================================

        storedToken.setUsed(true);

        String newTokenId = UUID.randomUUID().toString();
        String newSecret = UUID.randomUUID().toString();
        String newHashed = BCrypt.hashpw(newSecret, BCrypt.gensalt());

        LocalDateTime newExpiry = now.plusDays(REFRESH_TOKEN_DAYS);

        LocalDateTime maxSessionExpiry =
                storedToken.getSessionStart().plusDays(MAX_SESSION_DAYS);

        if (newExpiry.isAfter(maxSessionExpiry)) {
            newExpiry = maxSessionExpiry;
        }

        RefreshToken newToken = RefreshToken.builder()
                .tokenId(newTokenId)
                .tokenHash(newHashed)
                .familyId(storedToken.getFamilyId())
                .parentToken(storedToken)
                .expiryDate(newExpiry)
                .sessionStart(storedToken.getSessionStart())
                .revoked(false)
                .used(false)
                .user(storedToken.getUser())
                .build();

        refreshTokenRepository.save(newToken);

        storedToken.setReplacedByToken(newToken);
        refreshTokenRepository.save(storedToken);

        // =====================================================
        // NEW ACCESS TOKEN
        // =====================================================

        String newAccessToken = jwtService.generateToken(storedToken.getUser());

        long secondsUntilExpiry =
                Duration.between(now, newExpiry).getSeconds();

        addAccessCookie(response, newAccessToken);
        addRefreshCookie(response, newTokenId, newSecret, (int) secondsUntilExpiry);
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

        // Obtener familyId desde refresh cookie
        String refreshRaw = extractCookie(request, "refresh_token");
        String currentFamilyId = null;

        if (refreshRaw != null && refreshRaw.contains(".")) {

            String tokenId = refreshRaw.split("\\.")[0];

            RefreshToken token = refreshTokenRepository
                    .findByTokenId(tokenId)
                    .orElse(null);

            if (token != null) {
                currentFamilyId = token.getFamilyId();
            }
        }

        return getActiveSessions(user, currentFamilyId);
    }
    
    @Transactional
    public void revokeSession(String familyId, HttpServletRequest request) {

        String accessToken = extractAccessToken(request);

        String username = jwtService.extractUsername(accessToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<RefreshToken> tokens =
                refreshTokenRepository.findByFamilyId(familyId);

        if (tokens.isEmpty()) {
            throw new ResourceNotFoundException("Session not found");
        }

        // Validar que la sesión pertenezca al usuario
        if (!tokens.get(0).getUser().getId().equals(user.getId())) {
            throw new InvalidTokenException("Unauthorized session");
        }

        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(tokens);
    }
    
    private List<ActiveSessionResponse> getActiveSessions(User user, String currentFamilyId) {

        List<RefreshToken> tokens =
                refreshTokenRepository.findByUserAndRevokedFalse(user);

        return tokens.stream()
                .filter(token -> token.getParentToken() == null) 
                // solo tokens raíz de cada sesión
                .map(token -> ActiveSessionResponse.builder()
                        .familyId(token.getFamilyId())
                        .sessionStart(token.getSessionStart())
                        .expiryDate(token.getExpiryDate())
                        .ipAddress(token.getIpAddress())
                        .userAgent(token.getUserAgent())
                        .current(token.getFamilyId().equals(currentFamilyId))
                        .build()
                )
                .toList();
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
        cookie.setPath("/api/auth/refresh");
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
    
    private RefreshToken createRefreshToken(User user, String ip, String userAgent) {
    	
        String familyId = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();
        String hashedSecret = BCrypt.hashpw(secret, BCrypt.gensalt());
        
        DeviceInfo deviceInfo = deviceDetectorService.detect(userAgent);

        LocalDateTime now = LocalDateTime.now();

        String fingerprint =
                deviceFingerprintService.generateFingerprint(ip, userAgent);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .tokenHash(hashedSecret)
                .familyId(familyId)
                .deviceName(deviceInfo.getDevice())
                .browser(deviceInfo.getBrowser())
                .expiryDate(now.plusDays(REFRESH_TOKEN_DAYS))
                .sessionStart(now)
                .revoked(false)
                .used(false)
                .ipAddress(ip)
                .userAgent(userAgent)
                .deviceFingerprint(fingerprint)
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        // Guardamos temporalmente el secret para cookie
        refreshToken.setRawSecret(secret);

        return refreshToken;
    }
}