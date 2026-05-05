package com.example.task_system.controller;

import com.example.task_system.model.Task;
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
    public void createTask(@RequestBody Task task) {
        service.createTask(task);
    }

    @GetMapping("/{id}")
    public void getTask(@PathVariable UUID id) {

    }
}
