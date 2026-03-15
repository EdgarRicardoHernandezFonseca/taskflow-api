package com.edgar.taskflow.security.jwt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.edgar.taskflow.repository.BlacklistedTokenRepository;

import jakarta.servlet.FilterChain;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private BlacklistedTokenRepository blacklistRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    void shouldContinueFilterWhenNoAuthorizationHeader() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldRejectBlacklistedToken() throws Exception {

        String token = "abc123";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(blacklistRepository.existsByToken(token))
                .thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(blacklistRepository).existsByToken(token);
    }

    @Test
    void shouldAuthenticateValidToken() throws Exception {

        String token = "validtoken";
        String username = "edgar";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(blacklistRepository.existsByToken(token))
                .thenReturn(false);

        when(jwtService.extractUsername(token))
                .thenReturn(username);

        when(jwtService.isTokenValid(token, username))
                .thenReturn(true);

        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(
                        User.withUsername(username)
                                .password("123")
                                .roles("USER")
                                .build()
                );

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService).loadUserByUsername(username);
        verify(filterChain).doFilter(request, response);
    }
}