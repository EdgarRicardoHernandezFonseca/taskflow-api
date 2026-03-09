package com.edgar.taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.edgar.taskflow.entity.SecurityEvent;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

}
