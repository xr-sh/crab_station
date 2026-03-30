package com.example.repository;

import com.example.entity.PlatformSpec;
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
public interface PlatformSpecRepository extends JpaRepository<PlatformSpec, UUID> {

    Optional<PlatformSpec> findByName(String name);

    boolean existsByName(String name);

    List<PlatformSpec> findByStatusOrderByCreatedAtDesc(Integer status);

    List<PlatformSpec> findAllByOrderByCreatedAtDesc();

    @Query("SELECT ps FROM PlatformSpec ps WHERE " +
           "(:name IS NULL OR ps.name LIKE %:name%) AND " +
           "(:status IS NULL OR ps.status = :status) " +
           "ORDER BY ps.createdAt DESC")
    Page<PlatformSpec> findByFilters(@Param("name") String name,
                                     @Param("status") Integer status,
                                     Pageable pageable);
}