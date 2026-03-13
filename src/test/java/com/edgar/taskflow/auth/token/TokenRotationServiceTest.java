package com.edgar.taskflow.auth.token;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenRotationServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenRotationService tokenRotationService;

    private RefreshToken oldToken;

    @BeforeEach
    void setUp() {

        oldToken = new RefreshToken();

        oldToken.setFamilyId("family123");

        User user = new User();
        user.setUsername("edgar");

        oldToken.setUser(user);

        oldToken.setBrowser("Chrome");
        oldToken.setDeviceName("Laptop");
        oldToken.setOs("Windows");

        oldToken.setIpAddress("127.0.0.1");
        oldToken.setUserAgent("Chrome-Agent");
    }

    @Test
    void shouldRotateRefreshToken() {

        when(refreshTokenRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken newToken =
                tokenRotationService.rotateToken(oldToken);

        assertTrue(oldToken.isUsed());

        assertNotNull(newToken);
        assertEquals("family123", newToken.getFamilyId());

        assertEquals(oldToken, newToken.getParentToken());
        assertEquals(newToken, oldToken.getReplacedByToken());

        assertEquals("Chrome", newToken.getBrowser());
        assertEquals("Laptop", newToken.getDeviceName());

        assertFalse(newToken.isUsed());
        assertFalse(newToken.isRevoked());

        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }
}
