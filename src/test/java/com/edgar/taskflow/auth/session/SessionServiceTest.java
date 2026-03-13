package com.edgar.taskflow.auth.session;

import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionService sessionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("edgar");
    }

    // =============================
    // GET USER SESSIONS TEST
    // =============================

    @Test
    void shouldReturnUserSessions() {

        RefreshToken token = new RefreshToken();
        token.setFamilyId("family1");

        ActiveSessionResponse response =
                ActiveSessionResponse.builder()
                        .familyId("family1")
                        .build();

        when(refreshTokenRepository.findByUserAndRevokedFalse(user))
                .thenReturn(List.of(token));

        when(sessionMapper.toResponse(token))
                .thenReturn(response);

        List<ActiveSessionResponse> result =
                sessionService.getUserSessions(user);

        assertEquals(1, result.size());
        assertEquals("family1", result.get(0).getFamilyId());

        verify(refreshTokenRepository)
                .findByUserAndRevokedFalse(user);

        verify(sessionMapper)
                .toResponse(token);
    }

    // =============================
    // REVOKE SESSION TEST
    // =============================

    @Test
    void shouldRevokeSession() {

        RefreshToken token1 = new RefreshToken();
        token1.setRevoked(false);

        RefreshToken token2 = new RefreshToken();
        token2.setRevoked(false);

        when(refreshTokenRepository.findByFamilyId("family123"))
                .thenReturn(List.of(token1, token2));

        sessionService.revokeSession("family123");

        assertTrue(token1.isRevoked());
        assertTrue(token2.isRevoked());

        verify(refreshTokenRepository)
                .saveAll(anyList());
    }
}
