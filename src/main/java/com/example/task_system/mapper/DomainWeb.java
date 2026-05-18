package com.example.task_system.mapper;

import com.example.task_system.dto.TaskResponse;
import com.example.task_system.model.Task;

public class DomainWeb {
    public static TaskResponse DomainToWeb(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getStatus(),
                task.getIdempotencyKey(),
                task.getPayload(),
                task.getRetryCount(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getProcessingStartedAt());
    }
}
