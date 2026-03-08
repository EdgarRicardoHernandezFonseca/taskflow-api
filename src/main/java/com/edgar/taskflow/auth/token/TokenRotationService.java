package com.edgar.taskflow.auth.token;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenRotationService {
	
	private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken rotateToken(RefreshToken oldToken) {

        oldToken.setUsed(true);
        refreshTokenRepository.save(oldToken);

        RefreshToken newToken = new RefreshToken();

        newToken.setTokenId(UUID.randomUUID());
        newToken.setFamilyId(oldToken.getFamilyId());
        newToken.setParentTokenId(oldToken.getTokenId());

        newToken.setUser(oldToken.getUser());

        newToken.setBrowser(oldToken.getBrowser());
        newToken.setDeviceName(oldToken.getDeviceName());
        newToken.setOs(oldToken.getOs());

        newToken.setIpAddress(oldToken.getIpAddress());
        newToken.setUserAgent(oldToken.getUserAgent());

        newToken.setSessionStart(oldToken.getSessionStart());
        newToken.setLastActivity(LocalDateTime.now());
        newToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        newToken.setRevoked(false);
        newToken.setUsed(false);

        return refreshTokenRepository.save(newToken);
    }
}
