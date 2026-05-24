package com.example.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "platform_packages")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformPackage {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id = UUID.randomUUID();

    @Column(name = "category", length = 10, nullable = true)
    private String category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "spec_count", nullable = false)
    private Integer specCount = 0;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(length = 500)
    private String remark;

    @OneToMany(mappedBy = "platformPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortNo ASC, createdAt ASC")
    @Builder.Default
    private List<PlatformPackageItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "platformPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortNo ASC, createdAt ASC")
    @Builder.Default
    private List<PlatformPackagePriceRule> priceRules = new ArrayList<>();

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
