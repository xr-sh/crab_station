package com.example.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePlatformPackageCostConfigRequest {

    @NotNull(message = "固定成本不能为空")
    @DecimalMin(value = "0.00", message = "固定成本不能小于0")
    private BigDecimal fixedCost;

    private String remark;
}
