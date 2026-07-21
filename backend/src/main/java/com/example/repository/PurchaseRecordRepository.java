package com.example.repository;

import com.example.entity.PurchaseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, UUID> {

    @Query("SELECT p FROM PurchaseRecord p WHERE " +
           "(:supplier IS NULL OR p.supplier LIKE %:supplier%) AND " +
           "(:startDate IS NULL OR p.purchaseDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.purchaseDate <= :endDate) " +
           "ORDER BY p.purchaseDate DESC, p.createdAt DESC")
    Page<PurchaseRecord> findByFilters(@Param("supplier") String supplier,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       Pageable pageable);

    @Query("SELECT COUNT(p), " +
           "COALESCE(SUM(p.totalWeight), 0), " +
           "COALESCE(SUM(p.totalAmount), 0) " +
           "FROM PurchaseRecord p WHERE " +
           "(:supplier IS NULL OR p.supplier LIKE %:supplier%) AND " +
           "(:startDate IS NULL OR p.purchaseDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.purchaseDate <= :endDate)")
    Object[] sumByFilters(@Param("supplier") String supplier,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);
}
