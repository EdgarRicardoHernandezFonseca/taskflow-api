package com.edgar.taskflow.service.impl;

import com.edgar.taskflow.dto.TaskRequestDTO;
import com.edgar.taskflow.dto.TaskResponseDTO;
import com.edgar.taskflow.entity.Task;
import com.edgar.taskflow.entity.TaskStatus;
import com.edgar.taskflow.entity.User;
import com.edgar.taskflow.repository.TaskRepository;
import com.edgar.taskflow.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskRequestDTO request;
    private User user;
    private Task task;

    @BeforeEach
    void setUp() {

        request = new TaskRequestDTO();
        request.setTitle("Test Task");
        request.setDescription("Description");
        request.setStatus(TaskStatus.TODO);
        request.setDueDate(LocalDate.now().plusDays(1));
        request.setUserId(1L);

        user = User.builder()
                .id(1L)
                .username("edgar")
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("edgar", null, List.of())
        );
    }

    @Test
    void shouldCreateTask() {

        Mockito.when(userRepository.findByUsername("edgar"))
                .thenReturn(Optional.of(user));

        Mockito.when(taskRepository.save(Mockito.any(Task.class)))
                .thenReturn(task);

        TaskResponseDTO response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Test Task", response.getTitle());

        Mockito.verify(taskRepository).save(Mockito.any(Task.class));
    }

    @Test
    void shouldGetAllTasks() {

        Mockito.when(taskRepository.findAll())
                .thenReturn(List.of(task));

        List<TaskResponseDTO> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
    }

    @Test
    void shouldGetTaskById() {

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponseDTO result = taskService.getTaskById(1L);

        assertEquals("Test Task", result.getTitle());
    }

    @Test
    void shouldUpdateTask() {

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Mockito.when(taskRepository.save(Mockito.any(Task.class)))
                .thenReturn(task);

        TaskResponseDTO result = taskService.updateTask(1L, request);

        assertEquals("Test Task", result.getTitle());
    }

    @Test
    void shouldDeleteTask() {

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        Mockito.verify(taskRepository).delete(task);
    }

    @Test
    void shouldGetTasksForUser() {

        Page<Task> page = new PageImpl<>(List.of(task));

        Mockito.when(taskRepository.findByUserUsername(
                Mockito.eq("edgar"), Mockito.any()))
                .thenReturn(page);

        Page<TaskResponseDTO> result =
                taskService.getTasks(PageRequest.of(0,5));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldGetTasksForAdmin() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(() -> "ROLE_ADMIN"))
        );

        Page<Task> page = new PageImpl<>(List.of(task));

        Mockito.when(taskRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(page);

        Page<TaskResponseDTO> result =
                taskService.getTasks(PageRequest.of(0,5));

        assertEquals(1, result.getTotalElements());
    }
}