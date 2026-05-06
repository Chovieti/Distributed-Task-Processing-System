package com.example.task_system.repository;

import com.example.task_system.model.Task;
import com.example.task_system.model.TaskStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {
    // TODO Потихоньку от этого метода придется избавиться и заменять на отдельные
    //  для каждой необходимости изменения статуса задачи
    @Modifying
    @Query(value = """
    UPDATE Task t
    SET t.status = :newStatus,
        t.updatedAt = CURRENT_TIMESTAMP
    WHERE t.id = :id AND t.status = :oldStatus
    """)
    int updateStatusById(
            @Param("id") UUID id,
            @Param("oldStatus")TaskStatus oldStatus,
            @Param("newStatus")TaskStatus newStatus
    );

    // TODO Нужно разобраться как привести к единообразию
    //  и корректной работе ENUM без костыля в виде передачи их строкового значения
    // Метод взятия задачи в обработку - атомарно ищет и берёт не позволяя другим потоком перехватить
    @Query(value = """
    UPDATE tasks
    SET status = :newStatus,
        updated_at = CURRENT_TIMESTAMP,
        processing_started_at = CURRENT_TIMESTAMP
    WHERE id = (
        SELECT id FROM tasks
        WHERE status = :oldStatus
        ORDER BY created_at
        LIMIT 1
        FOR UPDATE SKIP LOCKED
    ) RETURNING id, status, idempotency_key, payload, retry_count, created_at, updated_at, processing_started_at
    """, nativeQuery = true)
    Optional<Task> findAndClaimTask(@Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus);

    // Находит старейшую застрявшую задачу
    @Query(value = """
    SELECT * FROM tasks
    WHERE status = 'PROCESSING'
        AND processing_started_at IS NOT NULL
        AND processing_started_at < CURRENT_TIMESTAMP - (:minutes * INTERVAL '1 minute')
    ORDER BY processing_started_at ASC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Task> findOldestStuckProcessingTask(@Param("minutes") int minutes);

    // Востанавливает задачу отправляя её в PENDING(всегда передается как параметр,
    // надо поискать способ лучше это задать) и увеличивая retryCount на 1
    @Modifying
    @Query(value = """
    UPDATE Task t
    SET t.status = :newStatus,
        t.updatedAt = CURRENT_TIMESTAMP,
        t.processingStartedAt = NULL,
        t.retryCount = t.retryCount + 1
    WHERE t.id = :id AND t.status = :oldStatus
    """)
    int reviveTask(
            @Param("id") UUID id,
            @Param("oldStatus") TaskStatus oldStatus,
            @Param("newStatus") TaskStatus newStatus
    );

    // Отправляет задачу в FAIL(всегда передается как параметр) если retryCount слишком большой
    @Modifying
    @Query(value = """
    UPDATE Task t
    SET t.status = :newStatus,
        t.updatedAt = CURRENT_TIMESTAMP,
        t.processingStartedAt = NULL
    WHERE t.id = :id AND t.status = :oldStatus
    """)
    int failTask(
            @Param("id") UUID id,
            @Param("oldStatus") TaskStatus oldStatus,
            @Param("newStatus") TaskStatus newStatus
    );

    // Находит задачу по ключу идемпотентности(который уникален как id, так что всегда будет максимум 1 задача)
    Optional<Task> findByIdempotencyKey(String key);
}
