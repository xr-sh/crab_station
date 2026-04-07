package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 快递分析数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpressAnalysisDTO {

    private UUID id;

    /**
     * 快递类别（顺丰/京东）
     */
    private String category;

    /**
     * 来源文件名
     */
    private String fileName;

    /**
     * Sheet名称
     */
    private String sheetName;

    /**
     * 行号
     */
    private Integer rowNum;

    /**
     * 动态字段（已解析为Map）
     */
    private Map<String, Object> dynamicFields;

    /**
     * 导入时间
     */
    private LocalDateTime importedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}