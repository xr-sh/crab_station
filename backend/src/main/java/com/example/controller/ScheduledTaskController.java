package com.example.controller;

import com.example.dto.*;
import com.example.entity.ScheduledTask;
import com.example.service.ScheduledTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduled-tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class ScheduledTaskController {

    private final ScheduledTaskService scheduledTaskService;

    @GetMapping
    public ResponseEntity<PageResponse<ScheduledTaskDTO>> getScheduledTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer status) {

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<ScheduledTask> taskPage = scheduledTaskService.getScheduledTasks(taskName, taskType, status, pageRequest);

        PageResponse<ScheduledTaskDTO> response = PageResponse.<ScheduledTaskDTO>builder()
                .content(taskPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .totalPages(taskPage.getTotalPages())
                .totalElements(taskPage.getTotalElements())
                .size(taskPage.getSize())
                .number(taskPage.getNumber())
                .first(taskPage.isFirst())
                .last(taskPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ScheduledTaskDTO>> getAllScheduledTasks() {
        List<ScheduledTask> tasks = scheduledTaskService.getAllScheduledTasks();
        List<ScheduledTaskDTO> dtos = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledTaskDTO> getScheduledTaskById(@PathVariable UUID id) {
        ScheduledTask task = scheduledTaskService.getScheduledTaskById(id);
        return ResponseEntity.ok(convertToDTO(task));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createScheduledTask(@Valid @RequestBody CreateScheduledTaskRequest request) {
        scheduledTaskService.createScheduledTask(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "定时任务创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateScheduledTask(@PathVariable UUID id, @Valid @RequestBody UpdateScheduledTaskRequest request) {
        scheduledTaskService.updateScheduledTask(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "定时任务更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteScheduledTask(@PathVariable UUID id) {
        scheduledTaskService.deleteScheduledTask(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "定时任务删除成功");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable UUID id) {
        scheduledTaskService.toggleStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "任务状态切换成功");
        return ResponseEntity.ok(response);
    }

    private ScheduledTaskDTO convertToDTO(ScheduledTask task) {
        return ScheduledTaskDTO.builder()
                .id(task.getId())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .cronExpression(task.getCronExpression())
                .taskParams(task.getTaskParams())
                .lastExecuteTime(task.getLastExecuteTime())
                .nextExecuteTime(task.getNextExecuteTime())
                .executeCount(task.getExecuteCount())
                .lastExecuteStatus(task.getLastExecuteStatus())
                .lastExecuteMessage(task.getLastExecuteMessage())
                .status(task.getStatus())
                .remark(task.getRemark())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}