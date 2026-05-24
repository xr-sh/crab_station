package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PlatformPackageDTO {
    private UUID id;
    private String name;
    private BigDecimal price;
    private BigDecimal fixedCost;
    private BigDecimal totalCost;
    private Integer status;
    private String remark;
    private List<PlatformPackageItemDTO> items;
    private List<PlatformPackagePriceRuleDTO> priceRules;
    private Integer specCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
