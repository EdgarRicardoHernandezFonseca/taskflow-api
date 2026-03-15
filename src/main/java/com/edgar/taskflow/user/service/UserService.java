package com.edgar.taskflow.user.service;

import com.edgar.taskflow.user.dto.UserRequestDTO;
import com.edgar.taskflow.user.dto.UserResponseDTO;

public interface UserService {

	UserResponseDTO createUser(UserRequestDTO request);
}
