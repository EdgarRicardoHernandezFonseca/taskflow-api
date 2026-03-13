package com.edgar.taskflow.auth.token;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import com.edgar.taskflow.security.device.DeviceInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private DeviceInfo device;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setUsername("edgar");

        device = new DeviceInfo(
                "Laptop",
                "Chrome",
                "Windows"
        );
    }

    @Test
    void shouldCreateRefreshToken() {

        when(refreshTokenRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(
                user,
                device,
                "127.0.0.1",
                "Chrome-Agent",
                "fingerprint123"
        );

        assertNotNull(token);
        assertNotNull(token.getTokenId());
        assertNotNull(token.getTokenHash());
        assertNotNull(token.getRawSecret());
        assertEquals(user, token.getUser());

        assertEquals("Chrome", token.getBrowser());
        assertEquals("Laptop", token.getDeviceName());
        assertEquals("Windows", token.getOs());

        assertEquals("127.0.0.1", token.getIpAddress());
        assertEquals("Chrome-Agent", token.getUserAgent());

        assertFalse(token.isRevoked());
        assertFalse(token.isUsed());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
}
