package com.edgar.taskflow.security;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edgar.taskflow.entity.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.Optional;


public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByToken(String token);

    boolean existsByToken(String token);
    
    void deleteByExpiryDateBefore(LocalDateTime dateTime);

}
