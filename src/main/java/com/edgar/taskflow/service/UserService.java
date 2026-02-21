package com.edgar.taskflow.service;

import com.edgar.taskflow.dto.UserRequestDTO;
import com.edgar.taskflow.dto.UserResponseDTO;

public interface UserService {

	UserResponseDTO createUser(UserRequestDTO request);
}
