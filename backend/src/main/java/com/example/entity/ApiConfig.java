package com.example.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "api_configs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiConfig {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id = UUID.randomUUID();

    @Column(name = "platform_name", nullable = false, length = 100)
    private String platformName;

    @Column(name = "api_key", length = 200)
    private String apiKey;

    @Column(name = "secret", length = 200)
    private String secret;

    @Column(name = "base_url", length = 500)
    private String baseUrl;

    @Column(name = "dynamic_config", columnDefinition = "TEXT")
    private String dynamicConfig;

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
