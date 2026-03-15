package com.edgar.taskflow.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.edgar.taskflow.user.dto.UserRequestDTO;
import com.edgar.taskflow.user.dto.UserResponseDTO;
import com.edgar.taskflow.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;

    @PostMapping
    public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO request) {
        return userService.createUser(request);
    }
}
