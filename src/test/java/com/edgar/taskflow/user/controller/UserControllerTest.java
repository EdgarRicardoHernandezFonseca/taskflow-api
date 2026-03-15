package com.edgar.taskflow.user.controller;

import com.edgar.taskflow.repository.BlacklistedTokenRepository;
import com.edgar.taskflow.user.controller.UserController;
import com.edgar.taskflow.user.dto.UserRequestDTO;
import com.edgar.taskflow.user.dto.UserResponseDTO;
import com.edgar.taskflow.user.service.UserService;
import com.edgar.taskflow.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUser() throws Exception {

        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("edgar");
        request.setEmail("edgar@email.com");
        request.setPassword("123456");

        UserResponseDTO response = UserResponseDTO.builder()
                .id(1L)
                .username("edgar")
                .email("edgar@email.com")
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(userService.createUser(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("edgar"))
                .andExpect(jsonPath("$.email").value("edgar@email.com"));
    }
}