package com.edgar.taskflow.task.controller;

import com.edgar.taskflow.entity.TaskStatus;
import com.edgar.taskflow.repository.BlacklistedTokenRepository;
import com.edgar.taskflow.task.dto.TaskRequestDTO;
import com.edgar.taskflow.task.dto.TaskResponseDTO;
import com.edgar.taskflow.task.service.TaskService;
import com.edgar.taskflow.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskResponseDTO buildResponse() {
        return TaskResponseDTO.builder()
                .id(1L)
                .title("Test Task")
                .description("Task description")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateTask() throws Exception {

    	TaskRequestDTO request = new TaskRequestDTO();
    	request.setTitle("Test Task");
    	request.setDescription("Task description");
    	request.setStatus(TaskStatus.TODO);
    	request.setDueDate(LocalDate.now().plusDays(1));
    	request.setUserId(1L);

        TaskResponseDTO response = buildResponse();

        Mockito.when(taskService.createTask(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void shouldGetAllTasks() throws Exception {

        TaskResponseDTO response = buildResponse();

        Page<TaskResponseDTO> page =
                new PageImpl<>(List.of(response), PageRequest.of(0,5),1);

        Mockito.when(taskService.getTasks(Mockito.any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Task"));
    }

    @Test
    void shouldGetTaskById() throws Exception {

        TaskResponseDTO response = buildResponse();

        Mockito.when(taskService.getTaskById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void shouldUpdateTask() throws Exception {

    	TaskRequestDTO request = new TaskRequestDTO();
    	request.setTitle("Updated Task");
    	request.setDescription("Updated description");
    	request.setStatus(TaskStatus.IN_PROGRESS);
    	request.setDueDate(LocalDate.now().plusDays(2));
    	request.setUserId(1L);

        TaskResponseDTO response = buildResponse();

        Mockito.when(taskService.updateTask(Mockito.eq(1L), Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteTask() throws Exception {

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(taskService).deleteTask(1L);
    }

}