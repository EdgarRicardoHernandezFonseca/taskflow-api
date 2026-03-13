package com.edgar.taskflow.auth.session;

import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.entity.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SessionMapperTest {

    private final SessionMapper sessionMapper = new SessionMapper();

    @Test
    void shouldMapRefreshTokenToActiveSessionResponse() {

        RefreshToken token = new RefreshToken();
        token.setFamilyId("family123");
        token.setSessionStart(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setIpAddress("127.0.0.1");
        token.setUserAgent("Chrome");
        token.setDeviceName("Laptop");
        token.setBrowser("Chrome");
        token.setLocation("Colombia");
        token.setLastActivity(LocalDateTime.now());

        ActiveSessionResponse response = sessionMapper.toResponse(token);

        assertEquals("family123", response.getFamilyId());
        assertEquals("127.0.0.1", response.getIpAddress());
        assertEquals("Chrome", response.getBrowser());
        assertEquals("Laptop", response.getDeviceName());
        assertEquals("Colombia", response.getLocation());
        assertFalse(response.isCurrent());
    }
}