package com.edgar.taskflow.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.edgar.taskflow.entity.TaskStatus;

@Data
@Builder
public class TaskResponseDTO {
	
	private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
