package com.edgar.taskflow.security;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {
	
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(unique = true, nullable = false)
	    private String token;

	    private LocalDateTime blacklistedAt;
	    
	    private LocalDateTime expiryDate;

	    @PrePersist
	    public void prePersist() {
	        this.blacklistedAt = LocalDateTime.now();
	    }
}
