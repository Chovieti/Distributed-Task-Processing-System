package com.example.task_system.dto;

import com.example.task_system.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        TaskStatus status,
        String idempotencyKey,
        String payload,
        int retryCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime processingStartedAt
) {}
