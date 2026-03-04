package com.edgar.taskflow.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.edgar.taskflow.entity.User;

public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {

	List<RefreshToken> findByUser(User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.familyId = :familyId")
    void revokeByFamilyId(@Param("familyId") String familyId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);
    
    Optional<RefreshToken> findByTokenId(String tokenId);
    
    List<RefreshToken> findByFamilyId(String familyId);
}
