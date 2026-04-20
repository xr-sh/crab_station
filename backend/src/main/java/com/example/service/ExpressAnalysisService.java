package com.example.service;

import com.example.dto.ExpressAnalysisDTO;
import com.example.entity.ExpressAnalysis;
import com.example.repository.ExpressAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 快递分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressAnalysisService {

    private final ExpressAnalysisRepository expressAnalysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 分页查询快递分析数据
     * 支持按类别、导入时间范围、收件地址模糊搜索、寄件时间范围筛选
     */
    @Transactional(readOnly = true)
    public Page<ExpressAnalysisDTO> getExpressAnalysisList(
            String category, 
            LocalDateTime startDate, 
            LocalDateTime endDate,
            String receiverAddress,
            LocalDate sentTimeStart,
            LocalDate sentTimeEnd,
            Pageable pageable) {
        
        // 转换日期格式为字符串（用于 JSON 字段查询）
        String sentTimeStartStr = sentTimeStart != null ? sentTimeStart.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        String sentTimeEndStr = sentTimeEnd != null ? sentTimeEnd.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        
        Page<ExpressAnalysis> page = expressAnalysisRepository.findByFilters(
                category, startDate, endDate, receiverAddress, sentTimeStartStr, sentTimeEndStr, pageable);
        
        return page.map(this::convertToDTO);
    }

    /**
     * 根据ID获取单条记录
     */
    @Transactional(readOnly = true)
    public ExpressAnalysisDTO getById(UUID id) {
        ExpressAnalysis entity = expressAnalysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        return convertToDTO(entity);
    }

    /**
     * 获取所有列名（动态字段名）
     */
    @Transactional(readOnly = true)
    public List<String> getAllColumnNames() {
        // 从所有记录中提取动态字段的key
        List<ExpressAnalysis> allRecords = expressAnalysisRepository.findAll();
        
        return allRecords.stream()
                .flatMap(record -> {
                    try {
                        Map<String, Object> fields = objectMapper.readValue(
                                record.getDynamicFields(), 
                                new TypeReference<Map<String, Object>>() {}
                        );
                        return fields.keySet().stream();
                    } catch (JsonProcessingException e) {
                        log.error("解析动态字段失败", e);
                        return java.util.stream.Stream.empty();
                    }
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取所有文件名
     */
    @Transactional(readOnly = true)
    public List<String> getAllFileNames() {
        return expressAnalysisRepository.findAllFileNames();
    }

    /**
     * 获取所有类别
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return expressAnalysisRepository.findAllCategories();
    }

    /**
     * 删除单条记录
     */
    @Transactional
    public void delete(UUID id) {
        ExpressAnalysis entity = expressAnalysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        expressAnalysisRepository.delete(entity);
    }

    /**
     * 清空所有数据
     * 使用批量删除避免乐观锁冲突
     */
    @Transactional
    public void clearAll() {
        expressAnalysisRepository.deleteAllInBatch();
        log.info("已清空所有快递分析数据");
    }

    /**
     * 删除指定文件名的所有记录
     */
    @Transactional
    public void deleteByFileName(String fileName) {
        expressAnalysisRepository.deleteByFileName(fileName);
        log.info("已删除文件 {} 的所有记录", fileName);
    }

    /**
     * 统计总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return expressAnalysisRepository.count();
    }

    /**
     * 按类别统计数量
     */
    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        return expressAnalysisRepository.countByCategory(category);
    }

    /**
     * 计算所有记录的平均运费
     */
    @Transactional(readOnly = true)
    public Double getAverageFee() {
        return expressAnalysisRepository.findAverageFee();
    }

    /**
     * 转换为DTO
     */
    private ExpressAnalysisDTO convertToDTO(ExpressAnalysis entity) {
        Map<String, Object> dynamicFields = new HashMap<>();
        
        try {
            if (entity.getDynamicFields() != null) {
                dynamicFields = objectMapper.readValue(
                        entity.getDynamicFields(), 
                        new TypeReference<Map<String, Object>>() {}
                );
            }
        } catch (JsonProcessingException e) {
            log.error("解析动态字段失败", e);
        }

        return ExpressAnalysisDTO.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .fileName(entity.getFileName())
                .sheetName(entity.getSheetName())
                .rowNum(entity.getRowNum())
                .duration(entity.getDuration())
                .durationHours(entity.getDurationHours())
                .dynamicFields(dynamicFields)
                .importedAt(entity.getImportedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}