package com.edgar.taskflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.edgar.taskflow.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends JpaRepository<Task, Long> {

	Page<Task> findByUserUsername(String username, Pageable pageable);
}
