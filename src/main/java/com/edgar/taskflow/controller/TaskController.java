package com.edgar.taskflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.edgar.taskflow.service.TaskService;
import com.edgar.taskflow.dto.TaskRequestDTO;
import com.edgar.taskflow.dto.TaskResponseDTO;
import com.edgar.taskflow.entity.Task;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
	
	private final TaskService taskService;
	
	@PostMapping
	public TaskResponseDTO createTask(@Valid @RequestBody TaskRequestDTO request) {
        return taskService.createTask(request);
    }

	@GetMapping
	public Page<Task> getAllTasks(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "id") String sortBy,
	        @RequestParam(defaultValue = "asc") String direction
	) {

	    Sort sort = direction.equalsIgnoreCase("asc") ?
	            Sort.by(sortBy).ascending() :
	            Sort.by(sortBy).descending();

	    Pageable pageable = PageRequest.of(page, size, sort);

	    return taskService.getTasks(pageable);
	}
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO request) {

        return ResponseEntity.ok(taskService.updateTask(id, request));
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}