package com.edgar.taskflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.edgar.taskflow.entity.BlacklistedToken;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BlacklistedTokenRepositoryTest {

    @Autowired
    private BlacklistedTokenRepository repository;

    @Test
    void shouldSaveAndFindToken() {

        BlacklistedToken token = new BlacklistedToken();
        token.setToken("abc123");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        repository.save(token);

        assertTrue(repository.findByToken("abc123").isPresent());
    }

    @Test
    void shouldCheckIfTokenExists() {

        BlacklistedToken token = new BlacklistedToken();
        token.setToken("xyz789");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        repository.save(token);

        boolean exists = repository.existsByToken("xyz789");

        assertTrue(exists);
    }
}