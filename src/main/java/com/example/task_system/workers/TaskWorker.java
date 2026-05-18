package com.example.task_system.workers;

import com.example.task_system.model.Task;
import com.example.task_system.model.TaskStatus;
import com.example.task_system.service.TaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TaskWorker {
    private final TaskService service;
    // TODO убрать в будущем, когда будет нормальная работа с тасками
    private final Random rng;

    public TaskWorker(TaskService service) {
        this.service = service;
        rng = new Random();
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {
        Task task = service.findAndClaimNextTask();
        if (task == null) return;
        // TODO Заменить в будущем на нормальную обработку и проход по всем шагам задачи
        //  и в зависимости от этого ставить результат
        service.completeTask(task.getId(), (rng.nextBoolean() ? TaskStatus.DONE : TaskStatus.FAILED));
    }

    public void processOnce() {
        process();
    }
}
