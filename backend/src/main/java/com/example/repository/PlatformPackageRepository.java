package com.example.repository;

import com.example.entity.PlatformPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PlatformPackageRepository extends JpaRepository<PlatformPackage, UUID> {

    @Query("SELECT pp FROM PlatformPackage pp WHERE " +
            "(:name IS NULL OR pp.name LIKE %:name%) AND " +
            "(:status IS NULL OR pp.status = :status) " +
            "ORDER BY pp.createdAt DESC")
    Page<PlatformPackage> findByFilters(@Param("name") String name,
                                        @Param("status") Integer status,
                                        Pageable pageable);

    @Modifying
    @Query("DELETE FROM PlatformPackage pp WHERE pp.id = :id")
    int deleteByIdBulk(@Param("id") UUID id);

    @Modifying
    @Query(value = "DELETE FROM platform_packages WHERE name = :name AND created_at = :createdAt", nativeQuery = true)
    int deleteByNameAndCreatedAt(@Param("name") String name, @Param("createdAt") LocalDateTime createdAt);
}
