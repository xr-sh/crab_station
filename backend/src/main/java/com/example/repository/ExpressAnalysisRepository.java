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

@Repository
public interface ExpressAnalysisRepository extends JpaRepository<ExpressAnalysis, UUID> {

    @Query(value = "SELECT e.id, e.category, e.file_name, e.sheet_name, e.row_num, e.duration, e.duration_hours, " +
           "e.dynamic_fields, e.imported_at, e.created_at, e.updated_at FROM express_analysis e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.imported_at >= :startDate) AND " +
           "(:endDate IS NULL OR e.imported_at <= :endDate) AND " +
           "(:receiverAddress IS NULL OR e.dynamic_fields LIKE CONCAT('%', :receiverAddress, '%')) AND " +
           "(:sentTimeStart IS NULL OR e.dynamic_fields IS NOT NULL) AND " +
           "(:sentTimeEnd IS NULL OR e.dynamic_fields IS NOT NULL)",
           countQuery = "SELECT COUNT(*) FROM express_analysis e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.imported_at >= :startDate) AND " +
           "(:endDate IS NULL OR e.imported_at <= :endDate) AND " +
           "(:receiverAddress IS NULL OR e.dynamic_fields LIKE CONCAT('%', :receiverAddress, '%')) AND " +
           "(:sentTimeStart IS NULL OR e.dynamic_fields IS NOT NULL) AND " +
           "(:sentTimeEnd IS NULL OR e.dynamic_fields IS NOT NULL)",
           nativeQuery = true)
    Page<ExpressAnalysis> findByFilters(
            @Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("receiverAddress") String receiverAddress,
            @Param("sentTimeStart") String sentTimeStart,
            @Param("sentTimeEnd") String sentTimeEnd,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT JSON_KEYS(dynamic_fields) FROM express_analysis", nativeQuery = true)
    List<String> findAllColumnNames();

    long countByCategory(String category);

    @Query("SELECT DISTINCT e.category FROM ExpressAnalysis e WHERE e.category IS NOT NULL ORDER BY e.category")
    List<String> findAllCategories();

    long countByFileName(String fileName);

    @Modifying
    @Query("DELETE FROM ExpressAnalysis e WHERE e.fileName = :fileName")
    void deleteByFileName(@Param("fileName") String fileName);

    @Query("SELECT e.fileName FROM ExpressAnalysis e " +
           "WHERE e.importedAt IN (SELECT MAX(e2.importedAt) FROM ExpressAnalysis e2 WHERE e2.fileName = e.fileName) " +
           "ORDER BY e.importedAt DESC")
    List<String> findAllFileNames();

    @Modifying
    @Query("DELETE FROM ExpressAnalysis")
    void deleteAllInBatch();
}
