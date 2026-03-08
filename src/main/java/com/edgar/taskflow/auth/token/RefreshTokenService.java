package com.edgar.taskflow.auth.token;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.security.device.DeviceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(User user,
                                           DeviceInfo device,
                                           String ip,
                                           String userAgent) {

        RefreshToken token = new RefreshToken();

        token.setTokenId(UUID.randomUUID());
        token.setFamilyId(UUID.randomUUID());
        token.setUser(user);

        token.setBrowser(device.getBrowser());
        token.setDeviceName(device.getDeviceName());
        token.setOs(device.getOs());

        token.setIpAddress(ip);
        token.setUserAgent(userAgent);

        token.setSessionStart(LocalDateTime.now());
        token.setLastActivity(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        token.setRevoked(false);
        token.setUsed(false);

        return refreshTokenRepository.save(token);
    }
}
