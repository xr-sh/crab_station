package com.example.controller;

import com.example.dto.ExpressAnalysisDTO;
import com.example.dto.ImportResultDTO;
import com.example.dto.PageResponse;
import com.example.service.ExcelImportService;
import com.example.service.ExpressAnalysisService;
import com.example.util.PageRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/express")
@RequiredArgsConstructor
public class ExpressController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "imported_at", "created_at", "updated_at", "duration_hours", "category", "file_name"
    );

    private final ExcelImportService excelImportService;
    private final ExpressAnalysisService expressAnalysisService;

    @PostMapping("/import")
    public ResponseEntity<List<ImportResultDTO>> importExcel(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("category") String category,
            @RequestParam(value = "hasHeader", defaultValue = "true") boolean hasHeader) {

        log.info("Importing express files, count={}, category={}, hasHeader={}", files.length, category, hasHeader);
        return ResponseEntity.ok(excelImportService.importExcelFiles(files, category, hasHeader));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ExpressAnalysisDTO>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "imported_at") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String receiverAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sentTimeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sentTimeEnd) {

        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("importedAt", "imported_at");
        fieldMapping.put("createdAt", "created_at");
        fieldMapping.put("updatedAt", "updated_at");
        fieldMapping.put("durationHours", "duration_hours");
        fieldMapping.put("category", "category");
        fieldMapping.put("fileName", "file_name");

        String dbColumnName = fieldMapping.getOrDefault(sortBy, sortBy);
        PageRequest pageRequest = PageRequestUtils.of(page, size, dbColumnName, sortDirection, ALLOWED_SORT_FIELDS);

        Page<ExpressAnalysisDTO> pageResult = expressAnalysisService.getExpressAnalysisList(
                category, startDate, endDate, receiverAddress, sentTimeStart, sentTimeEnd, pageRequest
        );

        PageResponse<ExpressAnalysisDTO> response = PageResponse.<ExpressAnalysisDTO>builder()
                .content(pageResult.getContent())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .size(pageResult.getSize())
                .number(pageResult.getNumber())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpressAnalysisDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(expressAnalysisService.getById(id));
    }

    @GetMapping("/columns")
    public ResponseEntity<List<String>> getColumns() {
        return ResponseEntity.ok(expressAnalysisService.getAllColumnNames());
    }

    @GetMapping("/file-names")
    public ResponseEntity<List<String>> getFileNames() {
        return ResponseEntity.ok(expressAnalysisService.getAllFileNames());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(expressAnalysisService.getAllCategories());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        expressAnalysisService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearAll() {
        expressAnalysisService.clearAll();
        return ResponseEntity.ok(Map.of("message", "All express data cleared"));
    }

    @DeleteMapping("/file/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteByFileName(@PathVariable String fileName) {
        expressAnalysisService.deleteByFileName(fileName);
        return ResponseEntity.ok(Map.of("message", "File records deleted"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", expressAnalysisService.count());
        stats.put("fileCount", expressAnalysisService.getAllFileNames().size());
        stats.put("columnCount", expressAnalysisService.getAllColumnNames().size());

        Map<String, Long> categoryStats = new HashMap<>();
        categoryStats.put("\u987a\u4e30", expressAnalysisService.countByCategory("\u987a\u4e30"));
        categoryStats.put("\u4eac\u4e1c", expressAnalysisService.countByCategory("\u4eac\u4e1c"));
        stats.put("categoryStats", categoryStats);

        Double averageFee = expressAnalysisService.getAverageFee();
        stats.put("averageFee", averageFee != null ? Math.round(averageFee * 100) / 100.0 : 0.0);
        return ResponseEntity.ok(stats);
    }
}
