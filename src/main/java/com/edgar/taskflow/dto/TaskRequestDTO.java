package com.edgar.taskflow.dto;

import lombok.Data;
import java.time.LocalDate;
import com.edgar.taskflow.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Data
public class TaskRequestDTO {
	
	@NotBlank
	private String title;
	
	@NotBlank
    private String description;
    
	@NotNull
	private TaskStatus status;
    
	@NotNull
	private LocalDate dueDate;
}
