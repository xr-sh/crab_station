package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "scheduled_tasks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTask {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id = UUID.randomUUID();

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(name = "task_params", columnDefinition = "TEXT")
    private String taskParams;

    @Column(name = "last_execute_time")
    private LocalDateTime lastExecuteTime;

    @Column(name = "next_execute_time")
    private LocalDateTime nextExecuteTime;

    @Column(name = "execute_count")
    private Integer executeCount = 0;

    @Column(name = "last_execute_status", length = 20)
    private String lastExecuteStatus;

    @Column(name = "last_execute_message", length = 500)
    private String lastExecuteMessage;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(length = 500)
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}