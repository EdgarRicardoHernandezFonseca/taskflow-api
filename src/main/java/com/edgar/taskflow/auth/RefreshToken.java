package com.edgar.taskflow.auth;

import java.time.LocalDateTime;

import com.edgar.taskflow.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenHash; // 🔐 ahora guardamos hash

    private String familyId;  // 🔐 identifica la sesión

    @ManyToOne
    private RefreshToken parentToken; // token anterior

    @OneToOne
    private RefreshToken replacedByToken; // siguiente token

    private LocalDateTime expiryDate;

    private boolean revoked;
    private boolean used;

    @ManyToOne
    private User user;
}
