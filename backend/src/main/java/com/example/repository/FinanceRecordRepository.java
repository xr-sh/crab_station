package com.example.repository;

import com.example.entity.FinanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, UUID> {

    @Query("SELECT f FROM FinanceRecord f WHERE " +
           "(:type IS NULL OR f.type = :type) AND " +
           "(:keyword IS NULL OR LOWER(f.remark) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:startDate IS NULL OR f.recordDate >= :startDate) AND " +
           "(:endDate IS NULL OR f.recordDate <= :endDate) " +
           "ORDER BY f.recordDate DESC, f.createdAt DESC")
    Page<FinanceRecord> findByFilters(@Param("type") String type,
                                      @Param("keyword") String keyword,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN f.type = :incomeType THEN f.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN f.type = :expenseType THEN f.amount ELSE 0 END), 0) " +
           "FROM FinanceRecord f WHERE " +
           "(:type IS NULL OR f.type = :type) AND " +
           "(:keyword IS NULL OR LOWER(f.remark) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:startDate IS NULL OR f.recordDate >= :startDate) AND " +
           "(:endDate IS NULL OR f.recordDate <= :endDate)")
    Object[] sumByFilters(@Param("type") String type,
                          @Param("keyword") String keyword,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate,
                          @Param("incomeType") String incomeType,
                          @Param("expenseType") String expenseType);
}
