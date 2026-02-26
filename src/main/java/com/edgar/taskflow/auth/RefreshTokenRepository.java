package com.edgar.taskflow.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edgar.taskflow.entity.User;

public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
