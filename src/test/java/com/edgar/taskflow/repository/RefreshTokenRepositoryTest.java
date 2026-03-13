package com.edgar.taskflow.repository;

import com.edgar.taskflow.entity.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser() {

        User user = User.builder()
                .username("edgar")
                .email("edgar@email.com")
                .password("123456")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    private RefreshToken createToken(User user) {

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenId("token123")
                .tokenHash("hash")
                .familyId("family1")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .sessionStart(LocalDateTime.now())
                .revoked(false)
                .used(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    @Test
    void shouldFindTokensByUser() {

        User user = createUser();
        createToken(user);

        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);

        assertEquals(1, tokens.size());
    }

    @Test
    void shouldFindByTokenId() {

        User user = createUser();
        createToken(user);

        var token = refreshTokenRepository.findByTokenId("token123");

        assertTrue(token.isPresent());
        assertEquals("family1", token.get().getFamilyId());
    }

    @Test
    void shouldFindByFamilyId() {

        User user = createUser();
        createToken(user);

        List<RefreshToken> tokens = refreshTokenRepository.findByFamilyId("family1");

        assertEquals(1, tokens.size());
    }

    @Test
    void shouldRevokeAllByUser() {

        User user = createUser();
        RefreshToken token = createToken(user);

        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken updated = refreshTokenRepository.findById(token.getId()).get();

        assertTrue(updated.isRevoked());
    }

    @Test
    void shouldDeleteTokensByUser() {

        User user = createUser();
        createToken(user);

        refreshTokenRepository.deleteByUser(user);

        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);

        assertEquals(0, tokens.size());
    }

    @Test
    void shouldDeleteExpiredTokens() {

        User user = createUser();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenId("expiredToken")
                .tokenHash("hash")
                .familyId("family2")
                .expiryDate(LocalDateTime.now().minusDays(1))
                .sessionStart(LocalDateTime.now())
                .revoked(false)
                .used(false)
                .build();

        refreshTokenRepository.save(token);

        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());

        var result = refreshTokenRepository.findByTokenId("expiredToken");

        assertTrue(result.isEmpty());
    }
}
