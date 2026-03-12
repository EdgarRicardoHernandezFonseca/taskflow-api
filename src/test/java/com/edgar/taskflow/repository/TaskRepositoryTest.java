package com.edgar.taskflow.repository;

import com.edgar.taskflow.entity.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindTasksByUsername() {

        User user = User.builder()
                .username("edgar")
                .email("edgar@email.com")
                .password("123456")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Task task = Task.builder()
                .title("Test Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        taskRepository.save(task);

        Page<Task> result =
                taskRepository.findByUserUsername(
                        "edgar",
                        PageRequest.of(0,5)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Task", result.getContent().get(0).getTitle());
    }
}
