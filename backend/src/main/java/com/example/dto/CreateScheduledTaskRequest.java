package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateScheduledTaskRequest {

    @NotBlank(message = "Task name is required")
    @Size(max = 100, message = "Task name must be at most 100 characters")
    private String taskName;

    @NotBlank(message = "Task type is required")
    @Size(max = 50, message = "Task type must be at most 50 characters")
    private String taskType;

    @NotBlank(message = "Cron expression is required")
    @Size(max = 100, message = "Cron expression must be at most 100 characters")
    private String cronExpression;

    @Size(max = 2000, message = "Task params must be at most 2000 characters")
    private String taskParams;

    private Integer status;

    @Size(max = 500, message = "Remark must be at most 500 characters")
    private String remark;
}
