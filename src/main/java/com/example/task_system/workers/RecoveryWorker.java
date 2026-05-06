package com.example.task_system.workers;

import com.example.task_system.service.TaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecoveryWorker {
    private final TaskService service;

    public RecoveryWorker(TaskService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 60000)
    public void recover() {
        service.recoverStuckTask();
    }
}
