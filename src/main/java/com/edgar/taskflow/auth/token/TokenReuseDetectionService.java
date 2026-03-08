package com.edgar.taskflow.auth.token;

import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.exception.ReuseTokenException;
import org.springframework.stereotype.Service;

@Service
public class TokenReuseDetectionService {
	
	public void detectReuse(RefreshToken token) {

        if (token.isUsed()) {
            throw new ReuseTokenException("Refresh token reuse detected");
        }

        if (token.isRevoked()) {
            throw new ReuseTokenException("Token revoked");
        }

    }
}
