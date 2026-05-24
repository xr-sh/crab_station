package com.example.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "platform_package_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformPackageItem {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PlatformPackage platformPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_spec_id", nullable = false)
    private PlatformSpec platformSpec;

    @Column(name = "platform_spec_name", length = 100)
    private String platformSpecName;

    @Column(name = "platform_spec_category", length = 10)
    private String platformSpecCategory;

    @Column(name = "sort_no")
    private Integer sortNo = 0;

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
