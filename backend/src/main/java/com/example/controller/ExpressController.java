package com.example.controller;

import com.example.dto.ExpressAnalysisDTO;
import com.example.dto.ImportResultDTO;
import com.example.dto.PageResponse;
import com.example.service.ExcelImportService;
import com.example.service.ExpressAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 快递分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/express")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class ExpressController {

    private final ExcelImportService excelImportService;
    private final ExpressAnalysisService expressAnalysisService;

    /**
     * 导入Excel文件（支持多文件上传）
     */
    @PostMapping("/import")
    public ResponseEntity<List<ImportResultDTO>> importExcel(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("category") String category,
            @RequestParam(value = "hasHeader", defaultValue = "true") boolean hasHeader) {
        
        log.info("开始导入 {} 个文件, 类别: {}, 是否有表头: {}", files.length, category, hasHeader);
        
        List<ImportResultDTO> results = excelImportService.importExcelFiles(files, category, hasHeader);
        
        return ResponseEntity.ok(results);
    }

    /**
     * 分页查询快递分析数据
     */
    @GetMapping
    public ResponseEntity<PageResponse<ExpressAnalysisDTO>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "importedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Sort sort = Sort.by(
                sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, 
                sortBy
        );
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<ExpressAnalysisDTO> pageResult = expressAnalysisService.getExpressAnalysisList(
                category, startDate, endDate, pageRequest
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

    /**
     * 获取单条记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpressAnalysisDTO> getById(@PathVariable UUID id) {
        ExpressAnalysisDTO dto = expressAnalysisService.getById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * 获取所有列名（动态字段名）
     */
    @GetMapping("/columns")
    public ResponseEntity<List<String>> getColumns() {
        List<String> columns = expressAnalysisService.getAllColumnNames();
        return ResponseEntity.ok(columns);
    }

    /**
     * 获取所有文件名
     */
    @GetMapping("/file-names")
    public ResponseEntity<List<String>> getFileNames() {
        List<String> fileNames = expressAnalysisService.getAllFileNames();
        return ResponseEntity.ok(fileNames);
    }

    /**
     * 获取所有快递类别
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = expressAnalysisService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 删除单条记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        expressAnalysisService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 清空所有数据
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAll() {
        expressAnalysisService.clearAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "已清空所有数据");
        return ResponseEntity.ok(response);
    }

    /**
     * 删除指定文件的所有记录
     */
    @DeleteMapping("/file/{fileName}")
    public ResponseEntity<Map<String, String>> deleteByFileName(@PathVariable String fileName) {
        expressAnalysisService.deleteByFileName(fileName);
        Map<String, String> response = new HashMap<>();
        response.put("message", "已删除该文件的所有记录");
        return ResponseEntity.ok(response);
    }

    /**
     * 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", expressAnalysisService.count());
        stats.put("fileCount", expressAnalysisService.getAllFileNames().size());
        stats.put("columnCount", expressAnalysisService.getAllColumnNames().size());
        // 按类别统计
        Map<String, Long> categoryStats = new HashMap<>();
        categoryStats.put("顺丰", expressAnalysisService.countByCategory("顺丰"));
        categoryStats.put("京东", expressAnalysisService.countByCategory("京东"));
        stats.put("categoryStats", categoryStats);
        return ResponseEntity.ok(stats);
    }
}