package com.example.task_system.controller;

import com.example.task_system.dto.CreateTaskRequest;
import com.example.task_system.service.TaskService;
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
    public void createTask(@RequestBody CreateTaskRequest request) {
        service.createTask(request);
    }

    @GetMapping("/{id}")
    public void getTask(@PathVariable UUID id) {

    }
}
