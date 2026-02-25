package com.edgar.taskflow.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByToken(String token);

    boolean existsByToken(String token);

}
