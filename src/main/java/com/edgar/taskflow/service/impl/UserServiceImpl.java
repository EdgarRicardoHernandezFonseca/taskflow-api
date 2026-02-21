package com.edgar.taskflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.service.UserService;
import com.edgar.taskflow.dto.UserRequestDTO;
import com.edgar.taskflow.dto.UserResponseDTO;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // luego encriptamos
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return UserResponseDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
}
