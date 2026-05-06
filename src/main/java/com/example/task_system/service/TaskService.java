package com.example.task_system.service;

import com.example.task_system.dto.CreateTaskRequest;
import com.example.task_system.model.Task;
import com.example.task_system.model.TaskStatus;
import com.example.task_system.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


// TODO Может затем выделить интерфейс причем может даже два, один для работы с эндпоинтами контролера, другой для работы с воркерами?
@Service
public class TaskService {
    private static final int MAX_RETRIES = 3;
    @Value("${task.retry.timeout.minutes}")
    private int retryMinutes;
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void createTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setPayload(request.payload());
        repository.save(task);
    }

    public Task getTask(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @Transactional
    public Task findAndClaimNextTask() {
        Optional<Task> opt = repository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING);
        if (opt.isEmpty()) return null;

        Task task = opt.get();
        int update = repository.claimTask(task.getId(), TaskStatus.PENDING, TaskStatus.PROCESSING);
        if (update == 0) {
            logger.debug("Failed to claim task {}", task.getId());
            return null;
        }
        task.setStatus(TaskStatus.PROCESSING);
        return task;
    }

    @Transactional
    public boolean completeTask(UUID id, TaskStatus newStatus) {
        boolean success = repository.updateStatusById(id, TaskStatus.PROCESSING, newStatus) == 1;
        if (!success) logger.warn("Failed to complete task {}", id);
        return success;
    }

    @Transactional
    public boolean recoverStuckTask() {
        Optional<Task> opt = repository.findOldestStuckProcessingTask(retryMinutes);
        if (opt.isEmpty()) return false;

        Task task = opt.get();
        int updated;
        if (task.getRetryCount() >= MAX_RETRIES) {
            updated = repository.failTask(task.getId(), TaskStatus.PROCESSING, TaskStatus.FAILED);
        } else {
            updated = repository.reviveTask(task.getId(), TaskStatus.PROCESSING, TaskStatus.PENDING);
        }
        if (updated == 1) {
            logger.info("Recovery task {}", task.getId());
        } else {
            logger.debug("Recovery skipped for task {}", task.getId());
        }
        return updated == 1;
    }
}
