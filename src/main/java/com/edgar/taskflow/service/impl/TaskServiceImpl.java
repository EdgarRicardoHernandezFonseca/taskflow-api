package com.edgar.taskflow.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.edgar.taskflow.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;
	
	private final UserRepository userRepository;

	@Override
	public TaskResponseDTO createTask(TaskRequestDTO request) {

	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String username = auth.getName();

	    User user = userRepository.findByUsername(username)
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
    
    @Override
    public TaskResponseDTO getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .build();
    }
    
    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);

        return TaskResponseDTO.builder()
                .id(updatedTask.getId())
                .title(updatedTask.getTitle())
                .description(updatedTask.getDescription())
                .status(updatedTask.getStatus())
                .dueDate(updatedTask.getDueDate())
                .createdAt(updatedTask.getCreatedAt())
                .build();
    }
    
    @Override
    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        taskRepository.delete(task);
    }
    
    @Override
    public Page<Task> getTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    
    public List<Task> getTasksForCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return taskRepository.findAll();
        }

        return taskRepository.findByUserUsername(username);
    }
    
   
}
