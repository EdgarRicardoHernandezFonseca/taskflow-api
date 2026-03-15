package com.edgar.taskflow.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import com.edgar.taskflow.entity.Role;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.user.dto.UserRequestDTO;
import com.edgar.taskflow.user.dto.UserResponseDTO;

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
                .role(Role.USER)
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
