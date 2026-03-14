package com.edgar.taskflow.security;

import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void shouldLoadUserByUsername() {

        User user = new User();
        user.setUsername("edgar");
        user.setPassword("123");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("edgar"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                service.loadUserByUsername("edgar");

        assertEquals("edgar", userDetails.getUsername());
        assertEquals("123", userDetails.getPassword());

        verify(userRepository).findByUsername("edgar");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByUsername("edgar"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("edgar")
        );
    }
}