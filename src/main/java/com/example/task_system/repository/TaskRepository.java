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
}
