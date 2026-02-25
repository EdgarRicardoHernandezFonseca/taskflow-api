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

	 @Column(unique = true)
	 private String token;

	 private LocalDateTime blacklistedAt;
}
