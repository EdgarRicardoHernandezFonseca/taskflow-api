package com.edgar.taskflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
		http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers
            .frameOptions(frame -> frame.disable()) // 🔥 IMPORTANTE para H2
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**").permitAll() // 🔥 permitir H2
            .anyRequest().permitAll()
        );

        return http.build();
    }
}
