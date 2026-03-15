package com.edgar.taskflow.user.service;

import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.user.dto.UserRequestDTO;
import com.edgar.taskflow.user.dto.UserResponseDTO;
import com.edgar.taskflow.user.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequestDTO request;

    @BeforeEach
    void setUp() {

        request = new UserRequestDTO();
        request.setUsername("edgar");
        request.setEmail("edgar@email.com");
        request.setPassword("123456");
    }

    @Test
    void shouldCreateUser() {

        User savedUser = User.builder()
                .id(1L)
                .username("edgar")
                .email("edgar@email.com")
                .password("123456")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(savedUser);

        UserResponseDTO response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("edgar", response.getUsername());
        assertEquals("edgar@email.com", response.getEmail());

        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }
}
