package com.edgar.taskflow.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edgar.taskflow.audit.entity.SecurityEvent;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

}
