package com.example.repository;

import com.example.entity.ApiConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiConfigRepository extends JpaRepository<ApiConfig, UUID> {

    Optional<ApiConfig> findByPlatformName(String platformName);

    boolean existsByPlatformName(String platformName);

    List<ApiConfig> findByStatusOrderByCreatedAtDesc(Integer status);

    List<ApiConfig> findAllByOrderByCreatedAtDesc();

    @Query("SELECT ac FROM ApiConfig ac WHERE " +
           "(:platformName IS NULL OR ac.platformName LIKE %:platformName%) AND " +
           "(:status IS NULL OR ac.status = :status) " +
           "ORDER BY ac.createdAt DESC")
    Page<ApiConfig> findByFilters(@Param("platformName") String platformName,
                                   @Param("status") Integer status,
                                   Pageable pageable);
}