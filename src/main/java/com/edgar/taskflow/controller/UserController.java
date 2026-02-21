package com.edgar.taskflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.edgar.taskflow.service.UserService;
import com.edgar.taskflow.dto.UserRequestDTO;
import com.edgar.taskflow.dto.UserResponseDTO;

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
