package com.example.service;

import com.example.dto.CreateScheduledTaskRequest;
import com.example.dto.UpdateScheduledTaskRequest;
import com.example.entity.ScheduledTask;
import com.example.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final ScheduledTaskRepository scheduledTaskRepository;

    @Transactional(readOnly = true)
    public Page<ScheduledTask> getScheduledTasks(String taskName, String taskType, Integer status, Pageable pageable) {
        return scheduledTaskRepository.findByFilters(taskName, taskType, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<ScheduledTask> getAllScheduledTasks() {
        return scheduledTaskRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Transactional(readOnly = true)
    public ScheduledTask getScheduledTaskById(UUID id) {
        return scheduledTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled task does not exist"));
    }

    @Transactional
    public ScheduledTask createScheduledTask(CreateScheduledTaskRequest request) {
        if (scheduledTaskRepository.existsByTaskName(request.getTaskName())) {
            throw new RuntimeException("Scheduled task name already exists");
        }

        ScheduledTask task = ScheduledTask.builder()
                .taskName(request.getTaskName())
                .taskType(request.getTaskType())
                .cronExpression(request.getCronExpression())
                .taskParams(request.getTaskParams())
                .executeCount(0)
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return scheduledTaskRepository.save(task);
    }

    @Transactional
    public ScheduledTask updateScheduledTask(UUID id, UpdateScheduledTaskRequest request) {
        ScheduledTask task = getScheduledTaskById(id);

        if (request.getTaskName() != null && !request.getTaskName().equals(task.getTaskName())) {
            if (scheduledTaskRepository.existsByTaskName(request.getTaskName())) {
                throw new RuntimeException("Scheduled task name already exists");
            }
            task.setTaskName(request.getTaskName());
        }

        if (request.getTaskType() != null) {
            task.setTaskType(request.getTaskType());
        }
        if (request.getCronExpression() != null) {
            task.setCronExpression(request.getCronExpression());
        }
        if (request.getTaskParams() != null) {
            task.setTaskParams(request.getTaskParams());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            task.setRemark(request.getRemark());
        }

        return scheduledTaskRepository.save(task);
    }

    @Transactional
    public void deleteScheduledTask(UUID id) {
        scheduledTaskRepository.delete(getScheduledTaskById(id));
    }

    @Transactional
    public ScheduledTask toggleStatus(UUID id) {
        ScheduledTask task = getScheduledTaskById(id);
        task.setStatus(task.getStatus() == 1 ? 0 : 1);
        return scheduledTaskRepository.save(task);
    }
}
