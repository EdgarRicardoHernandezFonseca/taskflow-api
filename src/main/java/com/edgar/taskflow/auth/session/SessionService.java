package com.edgar.taskflow.auth.session;

import com.edgar.taskflow.auth.dto.ActiveSessionResponse;
import com.edgar.taskflow.entity.RefreshToken;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {
	
	 	private final RefreshTokenRepository refreshTokenRepository;
	    private final SessionMapper sessionMapper;

	    public List<ActiveSessionResponse> getUserSessions(User user) {

	        List<RefreshToken> tokens =
	                refreshTokenRepository.findByUserAndRevokedFalse(user);

	        return tokens.stream()
	                .map(sessionMapper::toResponse)
	                .collect(Collectors.toList());
	    }

	    public void revokeSession(String familyId) {

	        List<RefreshToken> tokens =
	                refreshTokenRepository.findByFamilyId(familyId);

	        tokens.forEach(token -> token.setRevoked(true));

	        refreshTokenRepository.saveAll(tokens);
	    }

}
