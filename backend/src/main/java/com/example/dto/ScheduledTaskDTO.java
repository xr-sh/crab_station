package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ScheduledTaskDTO {
    private UUID id;
    private String taskName;
    private String taskType;
    private String cronExpression;
    private String taskParams;
    private LocalDateTime lastExecuteTime;
    private LocalDateTime nextExecuteTime;
    private Integer executeCount;
    private String lastExecuteStatus;
    private String lastExecuteMessage;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}