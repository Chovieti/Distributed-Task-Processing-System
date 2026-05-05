package com.example.task_system.service;

import com.example.task_system.dto.CreateTaskRequest;
import com.example.task_system.model.Task;
import com.example.task_system.model.TaskStatus;
import com.example.task_system.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


// TODO Может затем выделить интерфейс причем может даже два, один для работы с эндпоинтами контролера, другой для работы с воркерами?
@Service
public class TaskService {
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
    public boolean completeTask(UUID id, TaskStatus newStatus) {
        return repository.updateStatusById(id, TaskStatus.PROCESSING, newStatus) == 1;
    }

    @Transactional
    public Task findAndClaimNextTask() {
        Optional<Task> opt = repository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING);
        if (opt.isEmpty()) return null;

        Task task = opt.get();
        int update = repository.updateStatusById(task.getId(), TaskStatus.PENDING, TaskStatus.PROCESSING);
        if (update == 0) return null;
        task.setStatus(TaskStatus.PROCESSING);
        update = repository.updateProcessingStartedAt(task.getId(), TaskStatus.PROCESSING);
        if (update == 0) return null;
        return task;
    }
}
