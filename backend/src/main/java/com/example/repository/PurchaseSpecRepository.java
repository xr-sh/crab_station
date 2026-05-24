package com.example.repository;

import com.example.entity.PurchaseSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseSpecRepository extends JpaRepository<PurchaseSpec, UUID> {

    List<PurchaseSpec> findByStatusOrderByCreatedAtDesc(Integer status);

    List<PurchaseSpec> findAllByOrderByCreatedAtDesc();

    @Query("SELECT ps FROM PurchaseSpec ps WHERE " +
           "(:name IS NULL OR ps.name LIKE %:name%) AND " +
           "(:status IS NULL OR ps.status = :status) " +
           "ORDER BY ps.createdAt DESC")
    Page<PurchaseSpec> findByFilters(@Param("name") String name,
                                     @Param("status") Integer status,
                                     Pageable pageable);
}
