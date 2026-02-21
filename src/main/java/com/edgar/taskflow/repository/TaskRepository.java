package com.edgar.taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.edgar.taskflow.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
