package com.edgar.taskflow.auth.token.risk;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.security.LoginAlertService;
import com.edgar.taskflow.security.device.DeviceInfo;
import com.edgar.taskflow.security.risk.ImpossibleTravelService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAnalysisServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ImpossibleTravelService impossibleTravelService;

    @Mock
    private LoginAlertService loginAlertService;

    @InjectMocks
    private RiskAnalysisService riskAnalysisService;

    private User user;
    private RefreshToken token;
    private DeviceInfo deviceInfo;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setUsername("edgar");

        token = new RefreshToken();
        token.setLocation("Bogota");
        token.setDeviceName("Laptop");
        token.setBrowser("Chrome");
        token.setOs("Windows");
        token.setLastActivity(LocalDateTime.now());

        deviceInfo = new DeviceInfo("Laptop", "Chrome", "Windows");
    }

    @Test
    void shouldReturnWhenNoPreviousSessions() {

        when(refreshTokenRepository.findByUserAndRevokedFalse(user))
                .thenReturn(List.of());

        riskAnalysisService.analyzeLogin(user, "127.0.0.1", "Bogota", deviceInfo);

        verifyNoInteractions(impossibleTravelService);
        verifyNoInteractions(loginAlertService);
    }

    @Test
    void shouldTriggerImpossibleTravelAlert() {

        when(refreshTokenRepository.findByUserAndRevokedFalse(user))
                .thenReturn(List.of(token));

        when(impossibleTravelService.isImpossibleTravel(
                anyString(), anyString(), any()))
                .thenReturn(true);

        riskAnalysisService.analyzeLogin(user, "127.0.0.1", "Tokyo", deviceInfo);

        verify(loginAlertService)
                .sendSecurityAlert(eq(user),
                        contains("Impossible travel"));
    }

    @Test
    void shouldTriggerDeviceChangeAlert() {

        when(refreshTokenRepository.findByUserAndRevokedFalse(user))
                .thenReturn(List.of(token));

        DeviceInfo newDevice =
                new DeviceInfo("Mobile", "Safari", "iOS");

        when(impossibleTravelService.isImpossibleTravel(
                anyString(), anyString(), any()))
                .thenReturn(false);

        riskAnalysisService.analyzeLogin(user, "127.0.0.1", "Bogota", newDevice);

        verify(loginAlertService)
                .sendSecurityAlert(eq(user),
                        contains("New device"));
    }

    @Test
    void shouldNotSendAlertWhenNoRiskDetected() {

        when(refreshTokenRepository.findByUserAndRevokedFalse(user))
                .thenReturn(List.of(token));

        when(impossibleTravelService.isImpossibleTravel(
                anyString(), anyString(), any()))
                .thenReturn(false);

        riskAnalysisService.analyzeLogin(user, "127.0.0.1", "Bogota", deviceInfo);

        verify(loginAlertService, never())
                .sendSecurityAlert(any(), any());
    }
}