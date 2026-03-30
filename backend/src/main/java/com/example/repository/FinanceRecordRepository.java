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
           "(:startDate IS NULL OR f.recordDate >= :startDate) AND " +
           "(:endDate IS NULL OR f.recordDate <= :endDate) " +
           "ORDER BY f.recordDate DESC, f.createdAt DESC")
    Page<FinanceRecord> findByFilters(@Param("type") String type,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);
}
