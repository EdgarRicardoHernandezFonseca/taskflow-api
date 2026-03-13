package com.edgar.taskflow.auth.token;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.exception.ReuseTokenException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenReuseDetectionServiceTest {

    private final TokenReuseDetectionService service =
            new TokenReuseDetectionService();

    @Test
    void shouldThrowExceptionIfTokenUsed() {

        RefreshToken token = new RefreshToken();
        token.setUsed(true);

        assertThrows(
                ReuseTokenException.class,
                () -> service.detectReuse(token)
        );
    }

    @Test
    void shouldThrowExceptionIfTokenRevoked() {

        RefreshToken token = new RefreshToken();
        token.setRevoked(true);

        assertThrows(
                ReuseTokenException.class,
                () -> service.detectReuse(token)
        );
    }

    @Test
    void shouldPassIfTokenValid() {

        RefreshToken token = new RefreshToken();
        token.setUsed(false);
        token.setRevoked(false);

        assertDoesNotThrow(() -> service.detectReuse(token));
    }
}
