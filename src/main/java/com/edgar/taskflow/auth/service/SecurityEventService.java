package com.edgar.taskflow.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.edgar.taskflow.entity.SecurityEvent;
import com.edgar.taskflow.repository.SecurityEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityEventService {

    private final SecurityEventRepository repository;

    public void logEvent(String username,
                         String type,
                         String ip,
                         String device,
                         String location){

        SecurityEvent event = new SecurityEvent();

        event.setUsername(username);
        event.setEventType(type);
        event.setIpAddress(ip);
        event.setDevice(device);
        event.setLocation(location);
        event.setTimestamp(LocalDateTime.now());

        repository.save(event);
    }
}
