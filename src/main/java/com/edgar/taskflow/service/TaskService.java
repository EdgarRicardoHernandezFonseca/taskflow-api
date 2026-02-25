package com.edgar.taskflow.service;

import java.util.List;
import com.edgar.taskflow.dto.TaskRequestDTO;
import com.edgar.taskflow.dto.TaskResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.edgar.taskflow.entity.Task;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO request);

    List<TaskResponseDTO> getAllTasks();	

    Page<Task> getTasks(Pageable pageable);

    TaskResponseDTO getTaskById(Long id);

    TaskResponseDTO updateTask(Long id, TaskRequestDTO request);

    void deleteTask(Long id);
}
