package com.example.repository;

import com.example.entity.SpecificationMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecificationMappingRepository extends JpaRepository<SpecificationMapping, UUID> {

    @Query("SELECT sm FROM SpecificationMapping sm " +
           "JOIN FETCH sm.purchaseSpec ps " +
           "JOIN FETCH sm.platformSpec pls " +
           "WHERE " +
           "(:purchaseSpecName IS NULL OR ps.name LIKE %:purchaseSpecName%) AND " +
           "(:platformSpecName IS NULL OR pls.name LIKE %:platformSpecName%) AND " +
           "(:status IS NULL OR sm.status = :status) " +
           "ORDER BY sm.createdAt DESC")
    Page<SpecificationMapping> findByFilters(@Param("purchaseSpecName") String purchaseSpecName,
                                             @Param("platformSpecName") String platformSpecName,
                                             @Param("status") Integer status,
                                             Pageable pageable);

    boolean existsByPurchaseSpec_IdAndPlatformSpec_Id(UUID purchaseSpecId, UUID platformSpecId);

    Optional<SpecificationMapping> findByPurchaseSpec_IdAndPlatformSpec_Id(UUID purchaseSpecId, UUID platformSpecId);

    long countByPurchaseSpec_Id(UUID purchaseSpecId);

    long countByPlatformSpec_Id(UUID platformSpecId);
}
