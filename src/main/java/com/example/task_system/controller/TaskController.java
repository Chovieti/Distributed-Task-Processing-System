package com.example.task_system.controller;

import com.example.task_system.dto.CreateTaskRequest;
import com.example.task_system.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UUID> createTask(@RequestBody CreateTaskRequest request) {
        UUID id = service.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    // TODO Нужно будет решить, что будет возвращать контролер
    @GetMapping("/{id}")
    public void getTask(@PathVariable UUID id) {

    }
}
