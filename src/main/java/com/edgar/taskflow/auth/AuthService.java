package com.edgar.taskflow.auth;

import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.security.BlacklistedToken;
import com.edgar.taskflow.security.BlacklistedTokenRepository;
import com.edgar.taskflow.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	 private final BlacklistedTokenRepository blacklistedTokenRepository;
	 private final RefreshTokenRepository refreshTokenRepository;
	 private final UserRepository userRepository;
	 private final JwtService jwtService;
	
	 @Transactional
	 public void logout(HttpServletRequest request) {
		 final String authHeader = request.getHeader("Authorization");

	        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	            throw new RuntimeException("No token provided");
	        }

	        String token = authHeader.substring(7);

	        // 1️⃣ Agregar access token a blacklist
	        if (!blacklistedTokenRepository.existsByToken(token)) {
	        	blacklistedTokenRepository.save(
	        	        BlacklistedToken.builder()
	        	                .token(token)
	        	                .expiryDate(
	        	                    jwtService.extractExpiration(token)
	        	                            .toInstant()
	        	                            .atZone(ZoneId.systemDefault())
	        	                            .toLocalDateTime()
	        	                )
	        	                .build()
	        	);
	        }

	        // 2️⃣ Invalidar TODOS los refresh tokens del usuario
	        String username = jwtService.extractUsername(token);

	        User user = userRepository.findByUsername(username)
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        refreshTokenRepository.revokeAllByUser(user);
	 }
}
