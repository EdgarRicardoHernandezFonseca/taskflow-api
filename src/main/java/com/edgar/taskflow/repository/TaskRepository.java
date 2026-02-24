package com.edgar.taskflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.edgar.taskflow.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserUsername(String username);
}
