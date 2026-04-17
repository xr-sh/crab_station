package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ApiConfigDTO {
    private UUID id;
    private String platformName;
    private String apiKey;
    private String secret;
    private String baseUrl;
    private Map<String, Object> dynamicConfig;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}