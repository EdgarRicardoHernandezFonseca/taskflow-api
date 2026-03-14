package com.edgar.taskflow.auth.token.risk;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.security.LoginAlertService;
import com.edgar.taskflow.security.device.DeviceInfo;
import com.edgar.taskflow.security.risk.ImpossibleTravelService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RiskAnalysisService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final ImpossibleTravelService impossibleTravelService;
    private final LoginAlertService loginAlertService;

    public void analyzeLogin(
            User user,
            String ip,
            String location,
            DeviceInfo deviceInfo
    ) {

        List<RefreshToken> previousSessions =
                refreshTokenRepository.findByUserAndRevokedFalse(user);

        if (previousSessions.isEmpty()) {
            return;
        }

        Optional<RefreshToken> lastSession =
                previousSessions.stream()
                        .max(Comparator.comparing(RefreshToken::getLastActivity));

        if (lastSession.isEmpty()) {
            return;
        }

        RefreshToken lastToken = lastSession.get();

        detectImpossibleTravel(user, location, lastToken);

        detectDeviceChange(user, deviceInfo, lastToken);
    }

    // ======================================================
    // IMPOSSIBLE TRAVEL DETECTION
    // ======================================================

    private void detectImpossibleTravel(
            User user,
            String newLocation,
            RefreshToken lastToken
    ) {

        boolean impossibleTravel =
                impossibleTravelService.isImpossibleTravel(
                        lastToken.getLocation(),
                        newLocation,
                        lastToken.getLastActivity()
                );

        if (impossibleTravel) {

            loginAlertService.sendSecurityAlert(
                    user,
                    "Impossible travel detected between locations"
            );
        }
    }

    // ======================================================
    // DEVICE CHANGE DETECTION
    // ======================================================

    private void detectDeviceChange(
            User user,
            DeviceInfo deviceInfo,
            RefreshToken lastToken
    ) {

        boolean deviceChanged =
                !deviceInfo.getDevice().equals(lastToken.getDeviceName())
                || !deviceInfo.getBrowser().equals(lastToken.getBrowser())
                || !deviceInfo.getOs().equals(lastToken.getOs());

        if (deviceChanged) {

            loginAlertService.sendSecurityAlert(
                    user,
                    "New device detected for your account"
            );
        }
    }
}