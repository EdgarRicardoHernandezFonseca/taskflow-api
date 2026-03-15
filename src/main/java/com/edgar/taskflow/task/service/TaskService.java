package com.edgar.taskflow.task.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.edgar.taskflow.task.dto.TaskRequestDTO;
import com.edgar.taskflow.task.dto.TaskResponseDTO;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO request);

    List<TaskResponseDTO> getAllTasks();	

    Page<TaskResponseDTO> getTasks(Pageable pageable);

    TaskResponseDTO getTaskById(Long id);

    TaskResponseDTO updateTask(Long id, TaskRequestDTO request);

    void deleteTask(Long id);
}
