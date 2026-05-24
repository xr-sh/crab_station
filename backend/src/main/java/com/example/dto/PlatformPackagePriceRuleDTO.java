package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PlatformPackagePriceRuleDTO {
    private UUID id;
    private Integer qty;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private String costMode;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
