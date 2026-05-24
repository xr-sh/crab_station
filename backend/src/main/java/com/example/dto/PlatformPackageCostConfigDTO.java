package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlatformPackageCostConfigDTO {
    private BigDecimal fixedCost;
    private String remark;
}
