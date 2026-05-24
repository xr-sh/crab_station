package com.example.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 快递分析实体
 * 支持动态字段存储（JSON格式）
 */
@Data
@Entity
@Table(name = "express_analysis", indexes = {
    @Index(name = "idx_file_name", columnList = "file_name"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_imported_at", columnList = "imported_at")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpressAnalysis {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id = UUID.randomUUID();

    /**
     * 快递类别（顺丰/京东）
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 来源文件名
     */
    @Column(name = "file_name", length = 255)
    private String fileName;

    /**
     * Sheet名称
     */
    @Column(name = "sheet_name", length = 100)
    private String sheetName;

    /**
     * 行号（Excel中的行号）
     */
    @Column(name = "row_num")
    private Integer rowNum;

    /**
     * 时效（签收时间 - 寄件时间，格式：x天x小时）
     */
    @Column(name = "duration", length = 50)
    private String duration;

    /**
     * 时效小时数（用于排序）
     */
    @Column(name = "duration_hours")
    private Integer durationHours;

    /**
     * 动态字段（JSON格式存储）
     * 示例: {"快递单号":"SF123456", "发货时间":"2024-01-01", "收件人":"张三"}
     */
    @Column(name = "dynamic_fields", columnDefinition = "JSON")
    private String dynamicFields;

    /**
     * 导入时间
     */
    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (importedAt == null) {
            importedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
