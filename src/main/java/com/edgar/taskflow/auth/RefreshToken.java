package com.edgar.taskflow.auth;

import java.time.LocalDateTime;

import com.edgar.taskflow.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
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

    private LocalDateTime expiryDate;
    
    private String familyId;  // 🔐 identifica la sesión
    
    private boolean revoked;

    private LocalDateTime sessionStart;
    
    private String tokenHash; // 🔐 ahora guardamos hash
    
    @Column(unique = true)
    private String tokenId;
    
    private boolean used;
    
    @ManyToOne
    private RefreshToken parentToken; // token anterior

    @OneToOne
    private RefreshToken replacedByToken; // siguiente token

    @ManyToOne
    private User user;   
    
    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;
    
    private String deviceFingerprint;
    
    @Transient
    private String rawSecret;
    
    private String deviceName;
    
    private String deviceType;
    
    private String os;
    
    private String browser;
    
    private String location;
}
