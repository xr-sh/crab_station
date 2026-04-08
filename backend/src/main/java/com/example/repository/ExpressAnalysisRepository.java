package com.example.repository;

import com.example.entity.ExpressAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 快递分析数据访问层
 */
@Repository
public interface ExpressAnalysisRepository extends JpaRepository<ExpressAnalysis, UUID> {

    /**
     * 分页查询快递分析数据
     * 支持按类别、导入时间范围筛选
     */
    @Query("SELECT e FROM ExpressAnalysis e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.importedAt >= :startDate) AND " +
           "(:endDate IS NULL OR e.importedAt <= :endDate)")
    Page<ExpressAnalysis> findByFilters(
            @Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 获取所有不重复的列名
     * 从dynamicFields JSON中提取
     */
    @Query(value = "SELECT DISTINCT JSON_KEYS(dynamic_fields) FROM express_analysis", nativeQuery = true)
    List<String> findAllColumnNames();

    /**
     * 按类别统计记录数
     */
    long countByCategory(String category);

    /**
     * 获取所有不重复的类别
     */
    @Query("SELECT DISTINCT e.category FROM ExpressAnalysis e WHERE e.category IS NOT NULL ORDER BY e.category")
    List<String> findAllCategories();

    /**
     * 按文件名统计记录数
     */
    long countByFileName(String fileName);

    /**
     * 删除指定文件名的所有记录（批量删除）
     */
    @Modifying
    @Query("DELETE FROM ExpressAnalysis e WHERE e.fileName = :fileName")
    void deleteByFileName(@Param("fileName") String fileName);

    /**
     * 获取所有不重复的文件名（按最新导入时间排序）
     * 使用子查询避免 DISTINCT + ORDER BY 列不在 SELECT 中的 MySQL 限制
     */
    @Query("SELECT e.fileName FROM ExpressAnalysis e " +
           "WHERE e.importedAt IN (SELECT MAX(e2.importedAt) FROM ExpressAnalysis e2 WHERE e2.fileName = e.fileName) " +
           "ORDER BY e.importedAt DESC")
    List<String> findAllFileNames();

    /**
     * 批量删除所有记录（JPQL，直接执行 DELETE SQL，不经过 ORM 层）
     */
    @Modifying
    @Query("DELETE FROM ExpressAnalysis")
    void deleteAllInBatch();
}