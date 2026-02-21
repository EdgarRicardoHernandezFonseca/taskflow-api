package com.edgar.taskflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.edgar.taskflow.entity.Task;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.TaskRepository;
import com.edgar.taskflow.repository.UserRepository;
import com.edgar.taskflow.service.TaskService;
import com.edgar.taskflow.dto.TaskRequestDTO;
import com.edgar.taskflow.dto.TaskResponseDTO;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;
	
	private final UserRepository userRepository;

	@Override
	public TaskResponseDTO createTask(TaskRequestDTO request) {

	    User user = userRepository.findById(request.getUserId())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    Task task = Task.builder()
	            .title(request.getTitle())
	            .description(request.getDescription())
	            .status(request.getStatus())
	            .dueDate(request.getDueDate())
	            .createdAt(LocalDateTime.now())
	            .user(user)
	            .build();

	    Task savedTask = taskRepository.save(task);

	    return TaskResponseDTO.builder()
	            .id(savedTask.getId())
	            .title(savedTask.getTitle())
	            .description(savedTask.getDescription())
	            .status(savedTask.getStatus())
	            .dueDate(savedTask.getDueDate())
	            .createdAt(savedTask.getCreatedAt())
	            .build();
	}
    @Override
    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(task -> TaskResponseDTO.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .dueDate(task.getDueDate())
                        .createdAt(task.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
