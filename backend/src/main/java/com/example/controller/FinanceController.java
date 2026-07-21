package com.example.controller;

import com.example.dto.*;
import com.example.entity.FinanceRecord;
import com.example.service.FinanceRecordService;
import com.example.util.PageRequestUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class FinanceController {

    private final FinanceRecordService financeRecordService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("recordDate", "amount", "type", "createdAt", "updatedAt");

    @GetMapping
    public ResponseEntity<PageResponse<FinanceRecordDTO>> getFinanceRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PageRequest pageRequest = PageRequestUtils.of(page, size, sortBy, sortDirection, ALLOWED_SORT_FIELDS);

        Page<FinanceRecord> recordPage = financeRecordService.getFinanceRecords(type, keyword, startDate, endDate, pageRequest);

        PageResponse<FinanceRecordDTO> response = PageResponse.<FinanceRecordDTO>builder()
                .content(recordPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .totalPages(recordPage.getTotalPages())
                .totalElements(recordPage.getTotalElements())
                .size(recordPage.getSize())
                .number(recordPage.getNumber())
                .first(recordPage.isFirst())
                .last(recordPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<FinanceStatisticsDTO> getFinanceStatistics(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        FinanceStatisticsDTO statistics = financeRecordService.getFinanceStatistics(type, keyword, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinanceRecordDTO> getFinanceRecordById(@PathVariable UUID id) {
        FinanceRecord record = financeRecordService.getFinanceRecordById(id);
        return ResponseEntity.ok(convertToDTO(record));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createFinanceRecord(@Valid @RequestBody CreateFinanceRecordRequest request) {
        financeRecordService.createFinanceRecord(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "财务记录创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateFinanceRecord(@PathVariable UUID id, @Valid @RequestBody UpdateFinanceRecordRequest request) {
        financeRecordService.updateFinanceRecord(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "财务记录更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFinanceRecord(@PathVariable UUID id) {
        financeRecordService.deleteFinanceRecord(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "财务记录删除成功");
        return ResponseEntity.ok(response);
    }

    private FinanceRecordDTO convertToDTO(FinanceRecord record) {
        return FinanceRecordDTO.builder()
                .id(record.getId())
                .recordDate(record.getRecordDate())
                .amount(record.getAmount())
                .type(record.getType())
                .remark(record.getRemark())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
