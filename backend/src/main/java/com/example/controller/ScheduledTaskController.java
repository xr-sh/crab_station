package com.example.controller;

import com.example.dto.CreateScheduledTaskRequest;
import com.example.dto.PageResponse;
import com.example.dto.ScheduledTaskDTO;
import com.example.dto.UpdateScheduledTaskRequest;
import com.example.entity.ScheduledTask;
import com.example.service.ScheduledTaskService;
import com.example.util.PageRequestUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduled-tasks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ScheduledTaskController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt", "taskName", "taskType", "status");

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

        PageRequest pageRequest = PageRequestUtils.of(page, size, sortBy, sortDirection, ALLOWED_SORT_FIELDS);
        Page<ScheduledTask> taskPage = scheduledTaskService.getScheduledTasks(taskName, taskType, status, pageRequest);

        PageResponse<ScheduledTaskDTO> response = PageResponse.<ScheduledTaskDTO>builder()
                .content(taskPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()))
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
        return ResponseEntity.ok(scheduledTaskService.getAllScheduledTasks().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledTaskDTO> getScheduledTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(convertToDTO(scheduledTaskService.getScheduledTaskById(id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createScheduledTask(@Valid @RequestBody CreateScheduledTaskRequest request) {
        scheduledTaskService.createScheduledTask(request);
        return ResponseEntity.ok(Map.of("message", "Scheduled task created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateScheduledTask(@PathVariable UUID id, @Valid @RequestBody UpdateScheduledTaskRequest request) {
        scheduledTaskService.updateScheduledTask(id, request);
        return ResponseEntity.ok(Map.of("message", "Scheduled task updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteScheduledTask(@PathVariable UUID id) {
        scheduledTaskService.deleteScheduledTask(id);
        return ResponseEntity.ok(Map.of("message", "Scheduled task deleted successfully"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable UUID id) {
        scheduledTaskService.toggleStatus(id);
        return ResponseEntity.ok(Map.of("message", "Scheduled task status toggled successfully"));
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
