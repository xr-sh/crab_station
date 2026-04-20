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
     * 支持按类别、导入时间范围、收件地址模糊搜索、寄件时间范围筛选
     * 
     * 收件地址搜索：从 JSON 中提取地址字段，使用 COALESCE 获取第一个存在的地址值，然后 LIKE 模糊匹配
     * 寄件时间搜索：检查每个时间字段是否在区间内，只要有一个字段满足范围条件即可
     * 
     * 注意：nativeQuery 分页查询需要手动指定 countQuery，否则 Spring Data JPA 会生成无效的 count(e.*)
     */
    @Query(value = "SELECT e.id, e.category, e.file_name, e.sheet_name, e.row_num, e.duration, e.duration_hours, " +
           "e.dynamic_fields, e.imported_at, e.created_at, e.updated_at FROM express_analysis e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.imported_at >= :startDate) AND " +
           "(:endDate IS NULL OR e.imported_at <= :endDate) AND " +
           // 收件地址：提取地址字段后模糊匹配
           "(:receiverAddress IS NULL OR " +
           "  COALESCE(" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收件地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收件人地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收货地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收货人地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收件人详细地址\"')), " +
           "    ''" +
           "  ) LIKE CONCAT('%', :receiverAddress, '%')) AND " +
           // 寄件时间范围：检查每个时间字段是否在区间内（只要有一个满足即可）
           "(:sentTimeStart IS NULL OR :sentTimeEnd IS NULL OR " +
           "  (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件时间\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件时间\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件时间\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件日期\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件日期\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件日期\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货时间\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货时间\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货时间\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货日期\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货日期\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货日期\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出时间\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出时间\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出时间\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出日期\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出日期\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出日期\"')) <= :sentTimeEnd" +
           "  )" +
           ")",
           countQuery = "SELECT COUNT(*) FROM express_analysis e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.imported_at >= :startDate) AND " +
           "(:endDate IS NULL OR e.imported_at <= :endDate) AND " +
           "(:receiverAddress IS NULL OR " +
           "  COALESCE(" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收件地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收件人地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收货地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收货人地址\"')), " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"收件人详细地址\"')), " +
           "    ''" +
           "  ) LIKE CONCAT('%', :receiverAddress, '%')) AND " +
           "(:sentTimeStart IS NULL OR :sentTimeEnd IS NULL OR " +
           "  (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件时间\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件时间\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件时间\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件日期\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件日期\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"寄件日期\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货时间\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货时间\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货时间\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货日期\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货日期\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发货日期\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出时间\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出时间\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出时间\"')) <= :sentTimeEnd" +
           "  ) OR (" +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出日期\"')) IS NOT NULL AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出日期\"')) >= :sentTimeStart AND " +
           "    JSON_UNQUOTE(JSON_EXTRACT(e.dynamic_fields, '$.\"发出日期\"')) <= :sentTimeEnd" +
           "  )" +
           ")",
           nativeQuery = true)
    Page<ExpressAnalysis> findByFilters(
            @Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("receiverAddress") String receiverAddress,
            @Param("sentTimeStart") String sentTimeStart,
            @Param("sentTimeEnd") String sentTimeEnd,
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

    /**
     * 计算所有记录的平均运费
     * 从 dynamicFields JSON 中提取运费字段（运费/费用/快递费/物流费/快递费用）
     * 返回平均值（Double）
     */
    @Query(value = "SELECT AVG(CAST(" +
            "COALESCE(" +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"运费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"费用\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"快递费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"物流费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"快递费用\"')), " +
            "  NULL" +
            ") AS DECIMAL(10,2))) " +
            "FROM express_analysis " +
            "WHERE dynamic_fields IS NOT NULL " +
            "AND COALESCE(" +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"运费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"费用\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"快递费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"物流费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"快递费用\"'))" +
            ") IS NOT NULL " +
            "AND COALESCE(" +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"运费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"费用\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"快递费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"物流费\"')), " +
            "  JSON_UNQUOTE(JSON_EXTRACT(dynamic_fields, '$.\"快递费用\"'))" +
            ") != ''",
            nativeQuery = true)
    Double findAverageFee();
}