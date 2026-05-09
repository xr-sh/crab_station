package com.example.repository;

import com.example.entity.PlatformSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlatformSpecRepository extends JpaRepository<PlatformSpec, UUID> {

    List<PlatformSpec> findByStatusOrderByCreatedAtDesc(Integer status);

    List<PlatformSpec> findAllByOrderByCreatedAtDesc();

    @Query("SELECT ps FROM PlatformSpec ps WHERE " +
           "(:name IS NULL OR ps.name LIKE %:name%) AND " +
           "(:status IS NULL OR ps.status = :status) " +
           "ORDER BY ps.createdAt DESC")
    Page<PlatformSpec> findByFilters(@Param("name") String name,
                                     @Param("status") Integer status,
                                     Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE platform_specs " +
            "SET name = :name, category = :category, status = :status, remark = :remark, updated_at = NOW() " +
            "WHERE name = :oldName AND category = :oldCategory",
            nativeQuery = true)
    int updateByNameAndCategory(@Param("oldName") String oldName,
                       @Param("oldCategory") String oldCategory,
                       @Param("name") String name,
                       @Param("category") String category,
                       @Param("status") Integer status,
                       @Param("remark") String remark);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM platform_specs WHERE name = :name AND category = :category", nativeQuery = true)
    int deleteByNameAndCategory(@Param("name") String name, @Param("category") String category);
}
