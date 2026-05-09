package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "platform_specs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSpec {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id = UUID.randomUUID();

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String category;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(length = 500)
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
