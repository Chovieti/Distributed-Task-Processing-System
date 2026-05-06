package com.example.task_system.dto;

public record CreateTaskRequest(String payload, String idempotencyKey) {}
