package com.edgar.taskflow.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // 🔥 IGNORAR endpoints de auth
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        try {

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                if (blacklistedTokenRepository.existsByToken(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);

                if (username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority("ROLE_" + role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(authority)
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (ExpiredJwtException e) {
            System.out.println("Token expirado");
        }

        filterChain.doFilter(request, response);
    }
}
