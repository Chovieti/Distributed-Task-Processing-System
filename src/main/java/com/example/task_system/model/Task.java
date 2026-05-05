package com.example.task_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "retry_count", nullable = false)
    private int count;
    // Время создания таски
    @Column(name = "created_at",
            insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
    // Время обновления таски
    @Column(name = "updated_at",
            insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            nullable = false)
    private LocalDateTime updatedAt;
    // Время когда попало в обработку
    @Column(name = "processing_started_at",
            insertable = false)
    private LocalDateTime processingStartedAt;

    @PrePersist
    void init() {
        status = TaskStatus.PENDING;
        count = 0;
    }
}
