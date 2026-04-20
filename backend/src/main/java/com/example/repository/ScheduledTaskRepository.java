package com.example.repository;

import com.example.entity.ScheduledTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, UUID> {

    @Query("SELECT t FROM ScheduledTask t WHERE " +
           "(:taskName IS NULL OR t.taskName LIKE CONCAT('%', :taskName, '%')) AND " +
           "(:taskType IS NULL OR t.taskType = :taskType) AND " +
           "(:status IS NULL OR t.status = :status)")
    Page<ScheduledTask> findByFilters(
            @Param("taskName") String taskName,
            @Param("taskType") String taskType,
            @Param("status") Integer status,
            Pageable pageable);

    List<ScheduledTask> findByStatusOrderByCreatedAtDesc(Integer status);

    boolean existsByTaskName(String taskName);
}