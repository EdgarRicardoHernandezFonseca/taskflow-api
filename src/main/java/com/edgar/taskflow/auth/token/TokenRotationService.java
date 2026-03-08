package com.edgar.taskflow.auth.token;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCrypt;
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

        String secret = UUID.randomUUID().toString();
        String hash = BCrypt.hashpw(secret, BCrypt.gensalt());

        RefreshToken newToken = new RefreshToken();

        newToken.setTokenId(UUID.randomUUID().toString());
        newToken.setTokenHash(hash);
        newToken.setRawSecret(secret);

        newToken.setFamilyId(oldToken.getFamilyId());

        newToken.setParentToken(oldToken);
        oldToken.setReplacedByToken(newToken);

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
