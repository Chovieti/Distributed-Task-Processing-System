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

    Optional<Task> findFirstByStatusOrderByCreatedAtAsc(TaskStatus status);

    @Modifying
    @Query(value = """
    UPDATE Task t
    SET t.status = :newStatus,
        t.updatedAt = CURRENT_TIMESTAMP,
        t.processingStartedAt = CURRENT_TIMESTAMP
    WHERE t.id = :id AND t.status = :oldStatus
    """)
    int claimTask(
            @Param("id") UUID id,
            @Param("oldStatus") TaskStatus oldStatus,
            @Param("newStatus") TaskStatus newStatus
    );

    @Query(value = """
    SELECT * FROM tasks
    WHERE status = 'PROCESSING'
        AND processing_started_at IS NOT NULL
        AND processing_started_at < CURRENT_TIMESTAMP - (:minutes * INTERVAL '1 minute')
    ORDER BY processing_started_at ASC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Task> findOldestStuckProcessingTask(@Param("minutes") int minutes);

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
}
