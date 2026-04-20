package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateScheduledTaskRequest {
    
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100")
    private String taskName;

    @NotBlank(message = "任务类型不能为空")
    @Size(max = 50, message = "任务类型长度不能超过50")
    private String taskType;

    @NotBlank(message = "Cron表达式不能为空")
    @Size(max = 100, message = "Cron表达式长度不能超过100")
    private String cronExpression;

    @Size(max = 2000, message = "任务参数长度不能超过2000")
    private String taskParams;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}